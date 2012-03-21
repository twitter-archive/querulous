package com.twitter.querulous.async

import java.util.logging.{Logger, Level}
import java.util.concurrent.{Executors, RejectedExecutionException}
import java.util.concurrent.atomic.{AtomicInteger, AtomicBoolean}
import java.sql.Connection
import com.twitter.util.{Try, Throw, Future, Promise}
import com.twitter.util.{FuturePool, ExecutorServiceFuturePool, JavaTimer, TimeoutException}
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

class BlockingDatabaseWrapper(
  workPoolSize: Int,
  protected[async] val database: Database,
  stats: StatsCollector = NullStatsCollector)
extends AsyncDatabase {
  private val executor = Executors.newFixedThreadPool(workPoolSize, new DaemonThreadFactory("asyncWorkPool"))
  private val workPool = FuturePool(executor)

  def withConnection[R](f: Connection => R): Future[R] = {
    workPool {
      database.withConnection(f)
    }
  }

  // Equality overrides.
  override def equals(other: Any) = other match {
    case other: BlockingDatabaseWrapper => database eq other.database
    case _ => false
  }
  override def hashCode = database.hashCode
}
