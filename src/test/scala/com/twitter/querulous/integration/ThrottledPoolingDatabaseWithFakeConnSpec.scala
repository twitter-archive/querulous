package com.twitter.querulous.integration

import com.twitter.util.Time
import com.twitter.util.TimeConversions._
import com.twitter.querulous.query.SqlQueryFactory
import com.twitter.querulous.evaluator.StandardQueryEvaluatorFactory
import com.twitter.querulous.ConfiguredSpecification
import com.twitter.querulous.database.{Database, SqlDatabaseTimeoutException, ThrottledPoolingDatabaseFactory}
import com.twitter.querulous.test.sql.{FakeContext, FakeDriver}
import java.sql.SQLException

object ThrottledPoolingDatabaseWithFakeConnSpec {
  // configure repopulation interval to a minute to avoid conn repopulation when test running
  val testDatabaseFactory = new ThrottledPoolingDatabaseFactory(1, 1.second, 1.second, 60.seconds, Map.empty)
  val testQueryFactory = new SqlQueryFactory
  val testEvaluatorFactory = new StandardQueryEvaluatorFactory(testDatabaseFactory, testQueryFactory)
}

class ThrottledPoolingDatabaseWithFakeConnSpec extends ConfiguredSpecification {
  import ThrottledPoolingDatabaseWithFakeConnSpec._

  Database.driverName = FakeDriver.DRIVER_NAME

  val queryEvaluator = testEvaluatorFactory(config)
  var host = config.hostnames.mkString(",") + "/" + config.database

  FakeContext.setQueryResult(host, "SELECT 1 FROM DUAL", Array(Array[Any](1)))
  FakeContext.setQueryResult(host, "SELECT 2 FROM DUAL", Array(Array[Any](2)))

  "ThrottledJdbcPoolSpec" should {
    "execute some queries" >> {
      queryEvaluator.select("SELECT 1 FROM DUAL") { r => r.getInt(1) } mustEqual List(1)
      queryEvaluator.select("SELECT 2 FROM DUAL") { r => r.getInt(1) } mustEqual List(2)
    }


    "failfast after a host is down" >> {
      queryEvaluator.select("SELECT 1 FROM DUAL") { r => r.getInt(1) } mustEqual List(1)
      FakeContext.markServerDown(host)
      queryEvaluator.select("SELECT 1 FROM DUAL") { r => r.getInt(1) } must throwA[SQLException]
      val t0 = Time.now
      queryEvaluator.select("SELECT 1 FROM DUAL") { r => r.getInt(1) } must throwA[SQLException]
      System.err.println(Time.now - t0)
      (Time.now - t0).inMillis must beCloseTo(0L, 10000L)
    }

    "timeout when attempting to get a second connection" >> {
      queryEvaluator.select("SELECT 1 FROM DUAL") { r =>
        queryEvaluator.select("SELECT 2 FROM DUAL") { r2 => } must throwA[SqlDatabaseTimeoutException]
      }
    }
  }
}