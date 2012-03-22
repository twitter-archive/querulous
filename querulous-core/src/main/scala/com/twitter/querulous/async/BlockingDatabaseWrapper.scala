package com.twitter.querulous.async

import java.util.logging.{Logger, Level}
import java.util.concurrent.Executors
import java.sql.Connection
import com.twitter.util.{Future, FuturePool}
import com.twitter.querulous.{StatsCollector, NullStatsCollector, DaemonThreadFactory}
import com.twitter.querulous.database.{Database, DatabaseFactory}
import com.twitter.querulous.config

class BlockingDatabaseWrapperFactory(
  workPoolSize: Int,
  factory: DatabaseFactory)
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
      factory(hosts, name, username, password, urlOptions, driverName)
    )
  }
}

class BlockingDatabaseWrapper(
  workPoolSize: Int,
  protected[async] val database: Database)
extends AsyncDatabase {
  private val executor =
      Executors.newFixedThreadPool(workPoolSize, new DaemonThreadFactory("asyncWorkPool-" + database.hosts.mkString(",")))
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
