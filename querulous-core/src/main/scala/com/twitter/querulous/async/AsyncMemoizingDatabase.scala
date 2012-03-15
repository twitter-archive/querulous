package com.twitter.querulous.async

import scala.collection.mutable

/**
 *  Like MemoizingDatabaseFactory, but meant for async querulous.
 */
class AsyncMemoizingDatabaseFactory(val databaseFactory: AsyncDatabaseFactory) extends AsyncDatabaseFactory {
  // TODO: Use CacheBuilder after upgrading the Guava dependency to >= v10.
  private val databases = new mutable.HashMap[String, AsyncDatabase] with mutable.SynchronizedMap[String, AsyncDatabase]

  def apply(dbhosts: List[String], dbname: String, username: String, password: String, urlOptions: Map[String, String], driverName: String) = synchronized {
    databases.getOrElseUpdate(
      dbhosts.toString + "/" + dbname,
      databaseFactory(dbhosts, dbname, username, password, urlOptions, driverName))
  }

  // cannot memoize a connection without specifying a database
  override def apply(dbhosts: List[String], username: String, password: String) = databaseFactory(dbhosts, username, password)
}
