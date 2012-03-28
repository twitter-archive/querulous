package com.twitter.querulous.async

import java.util.logging.{Logger, Level}
import java.util.concurrent.{Executors, CancellationException, ThreadPoolExecutor, LinkedBlockingQueue, TimeUnit}
import java.util.concurrent.atomic.AtomicBoolean
import java.sql.Connection
import com.twitter.util.{Future, FuturePool, JavaTimer, TimeoutException}
import com.twitter.querulous.{StatsCollector, NullStatsCollector, DaemonThreadFactory}
import com.twitter.querulous.database.{Database, DatabaseFactory}
import com.twitter.querulous.config

class BlockingDatabaseWrapperFactory(
  workPoolSize: Int,
  maxWaiters: Int,
  factory: DatabaseFactory,
  stats: StatsCollector = NullStatsCollector)
extends AsyncDatabaseFactory {
  def apply(
    hosts: List[String],
    name: String,
    username: String,
    password: String,
    urlOptions: Map[String, String],
    driverName: String
  ): AsyncDatabase = {
    new BlockingDatabaseWrapper(
      workPoolSize,
      maxWaiters,
      factory(hosts, name, username, password, urlOptions, driverName),
      stats
    )
  }
}

private object AsyncConnectionCheckout {
  lazy val checkoutTimer = new JavaTimer(true)
}

class BlockingDatabaseWrapper(
  workPoolSize: Int,
  maxWaiters: Int,
  protected[async] val database: Database,
  stats: StatsCollector = NullStatsCollector)
extends AsyncDatabase {
  import AsyncConnectionCheckout._

  val dbStr = database.hosts.mkString(",") + database.name

  // Note: Our executor is similar to what you'd get via Executors.newFixedThreadPool(), but the latter
  // returns an ExecutorService, which unfortunately doesn't give us as much visibility into stats as
  // the ThreadPoolExecutor, so we create one ourselves.
  private val executor = {
    val e = new ThreadPoolExecutor(workPoolSize, workPoolSize, 0L, TimeUnit.MILLISECONDS,
                                   new LinkedBlockingQueue[Runnable](maxWaiters),
                                   new DaemonThreadFactory("asyncWorkPool-" + dbStr));
    stats.addGauge("db-async-active-threads-" + dbStr)(e.getActiveCount)
    stats.addGauge("db-async-waiters-" + dbStr)(e.getQueue.size)
    e
  }
  private val workPool = FuturePool(executor)
  private val openTimeout = database.openTimeout

  // We cache the connection checked out from the underlying database in a thread local so
  // that each workPool thread can hold on to a connection for its lifetime. This saves expensive
  // context switches in borrowing/returning connections from the underlying database per request.
  private val tlConnection = new ThreadLocal[Connection] {
    override def initialValue() = {
      stats.incr("db-async-cached-connection-acquire-total", 1)
      stats.incr("db-async-cached-connection-acquire-" + dbStr, 1)
      database.open()
    }
  }

  // Basically all we need to do is offload the real work to workPool. However, there is one
  // complication - enforcement of DB open timeout. If a connection is not available, most
  // likely neither is a thread to do the work, so requests would queue up in the future pool.
  // We need to ensure requests don't stick around in the queue for more than openTimeout duration.
  // To do this, we use a trick implemented in the ExecutorServiceFuturePool for cancellation - i.e.,
  // setup a timeout and cancel the request iff it hasn't already started executing, coordinating
  // via an AtomicBoolean.
  def withConnection[R](f: Connection => R): Future[R] = {
    val startCoordinator = new AtomicBoolean(true)
    val future = workPool {
      val isRunnable = startCoordinator.compareAndSet(true, false)
      if (isRunnable) {
        val connection = tlConnection.get()
        try {
          f(connection)
        } catch {
          case e => {
            // An exception occurred. To be safe, we return our cached connection back to the pool. This
            // protects us in case either the connection has been killed or our thread is going to be
            // terminated with an unhandled exception. If neither is the case (e.g. this was a benign
            // exception like a SQL constraint violation), it still doesn't hurt much to return/re-borrow
            // the connection from the underlying database, given that this should be rare.
            // TODO: Handle possible connection leakage if this thread is destroyed in some other way.
            // (Note that leaking an exception from here will not kill the thread since the FuturePool
            // will swallow it and wrap with a Throw()).
            stats.incr("db-async-cached-connection-release-total", 1)
            stats.incr("db-async-cached-connection-release-" + dbStr, 1)
            database.close(connection)
            tlConnection.remove()
            throw e
          }
        }
      } else {
        throw new CancellationException
      }
    }

    // If openTimeout elapsed and our task has still not started, cancel it and return the
    // exception. If not, rescue the exception with the *original* future, as if nothing
    // happened. Any other exception - just propagate unchanged.
    future.within(checkoutTimer, openTimeout) rescue {
      case e: TimeoutException => {
        val isCancellable = startCoordinator.compareAndSet(true, false)
        if (isCancellable) {
          stats.incr("db-async-open-timeout-total", 1)
          stats.incr("db-async-open-timeout-" + dbStr, 1)
          future.cancel()
          Future.exception(e)
        } else {
          future  // note: this is the original future not bounded by within().
        }
      }
    }
  }

  // Equality overrides.
  override def equals(other: Any) = other match {
    case other: BlockingDatabaseWrapper => database eq other.database
    case _ => false
  }
  override def hashCode = database.hashCode
}
