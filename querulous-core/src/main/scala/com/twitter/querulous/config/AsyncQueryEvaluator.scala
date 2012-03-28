package com.twitter.querulous.config

import com.twitter.util
import com.twitter.querulous
import com.twitter.querulous.async
import com.twitter.querulous.database.DatabaseFactory
import com.twitter.querulous.query.QueryFactory

abstract class AsyncQueryEvaluator {
  var database: Database     = new Database
  var query: Query           = new Query
  var singletonFactory       = false
  var maxWaiters             = Int.MaxValue

  // Size of the work pool used by the AsyncDatabase to do all the DB query work.
  // This should typically be the same size as the DB connection pool.
  var workPoolSize: Int

  private var memoizedFactory: Option[async.AsyncQueryEvaluatorFactory] = None

  // Optionally takes in a method to transform the QueryFactory we are going to use (typically used for stats collection).
  protected def newQueryFactory(stats: querulous.StatsCollector, queryStatsFactory: Option[QueryFactory => QueryFactory]) = {
    query(stats, queryStatsFactory)
  }

  // Optionally takes in a method to transform the DatabaseFactory we are going to use (typically used for stats collection).
  protected def newDatabaseFactory(stats: querulous.StatsCollector, dbStatsFactory: Option[DatabaseFactory => DatabaseFactory]) = {
    database(stats, dbStatsFactory)
  }

  def apply(): async.AsyncQueryEvaluatorFactory = apply(querulous.NullStatsCollector)

  def apply(stats: querulous.StatsCollector): async.AsyncQueryEvaluatorFactory = apply(stats, None, None)

  def apply(stats: querulous.StatsCollector, dbStatsFactory: DatabaseFactory => DatabaseFactory, queryStatsFactory: QueryFactory => QueryFactory): async.AsyncQueryEvaluatorFactory = apply(stats, Some(dbStatsFactory), Some(queryStatsFactory))

  def apply(stats: querulous.StatsCollector, dbStatsFactory: Option[DatabaseFactory => DatabaseFactory], queryStatsFactory: Option[QueryFactory => QueryFactory]): async.AsyncQueryEvaluatorFactory = {
    synchronized {
      if (!singletonFactory) memoizedFactory = None

      memoizedFactory = memoizedFactory orElse {
        var dbFactory: async.AsyncDatabaseFactory = new async.BlockingDatabaseWrapperFactory(
          workPoolSize,
          maxWaiters,
          newDatabaseFactory(stats, dbStatsFactory),
          stats
        )

        if (database.memoize) {
          // Ensure AsyncDatabase gets memoized.
          dbFactory = new async.AsyncMemoizingDatabaseFactory(dbFactory)
        }

        Some(new async.StandardAsyncQueryEvaluatorFactory(dbFactory, newQueryFactory(stats, queryStatsFactory)))
      }

      memoizedFactory.get
    }
  }
}
