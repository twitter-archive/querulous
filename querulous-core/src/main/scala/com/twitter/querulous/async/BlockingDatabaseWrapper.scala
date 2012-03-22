package com.twitter.querulous.async

import java.util.logging.{Logger, Level}
import java.util.concurrent.Executors
import java.sql.Connection
import com.twitter.util.{Future, FuturePool, JavaTimer, TimeoutException}
import com.twitter.querulous.{StatsCollector, NullStatsCollector, DaemonThreadFactory}
import com.twitter.querulous.database.{Database, DatabaseFactory}
import com.twitter.querulous.config

class BlockingDatabaseWrapperFactory(
  workPoolSize: Int,
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
  protected[async] val database: Database,
  stats: StatsCollector = NullStatsCollector)
extends AsyncDatabase {
  import AsyncConnectionCheckout._

  private val workPool = FuturePool(Executors.newFixedThreadPool(
      workPoolSize, new DaemonThreadFactory("asyncWorkPool-" + database.hosts.mkString(","))))
  private val openTimeout = database.openTimeout

  def withConnection[R](f: Connection => R): Future[R] = {
    val future = workPool {
      database.withConnection(f)
    }

    // We need to ensure open timeout is enforced correctly. If a thread is available immediately in workPool,
    // most likely a connection is available too, but if not, the underlying database is responsible for timing
    // out the connection open() call. But if a thread is *not* immediately available, we don't want our
    // request hanging there indefinitely, hence we use the mechanism below.
    future.within(checkoutTimer, openTimeout) onFailure {
      case e: TimeoutException => {
        stats.incr("db-async-open-timeout-count", 1)
        // Cancel the future. Note that the ExecutorServiceFuturePool only actually propagates the cancel
        // if the task hasn't run yet, which is exactly the behavior we want. Our goal is to abort requests
        // waiting too long for an available connection, not those that already got one and are already
        // executing (there is a separate query timeout mechanism for that).
        future.cancel()
      }

      case _ => {}
    }
  }

  // Equality overrides.
  override def equals(other: Any) = other match {
    case other: BlockingDatabaseWrapper => database eq other.database
    case _ => false
  }
  override def hashCode = database.hashCode
}
