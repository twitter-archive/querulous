package com.twitter.querulous.integration

import com.twitter.util.Time
import com.twitter.util.TimeConversions._
import com.twitter.querulous.evaluator.StandardQueryEvaluatorFactory
import com.twitter.querulous.ConfiguredSpecification
import com.twitter.querulous.sql.{FakeContext, FakeDriver}
import com.mysql.jdbc.exceptions.jdbc4.CommunicationsException
import com.twitter.querulous.database.{PoolEmptyException, Database, ThrottledPoolingDatabaseFactory}
import com.twitter.querulous.query.{SqlQueryTimeoutException, TimingOutQueryFactory, SqlQueryFactory}

object ThrottledPoolingDatabaseWithFakeConnSpec {
  // configure repopulation interval to a minute to avoid conn repopulation when test running
  val testDatabaseFactory = new ThrottledPoolingDatabaseFactory(1, 1.second, 1.second, 60.seconds, Map.empty)
  val testQueryFactory = new TimingOutQueryFactory(new SqlQueryFactory, 500.millis, true)
  val testEvaluatorFactory = new StandardQueryEvaluatorFactory(testDatabaseFactory, testQueryFactory)
}

class ThrottledPoolingDatabaseWithFakeConnSpec extends ConfiguredSpecification {
  import ThrottledPoolingDatabaseWithFakeConnSpec._

  doBeforeSpec { Database.driverName = FakeDriver.DRIVER_NAME }

  "ThrottledJdbcPoolSpec" should {
    val host = config.hostnames.mkString(",") + "/" + config.database

    FakeContext.setQueryResult(host, "SELECT 1 FROM DUAL", Array(Array[java.lang.Object](1.asInstanceOf[AnyRef])))
    FakeContext.setQueryResult(host, "SELECT 2 FROM DUAL", Array(Array[java.lang.Object](2.asInstanceOf[AnyRef])))
    "execute some queries" >> {
      val queryEvaluator = testEvaluatorFactory(config)

      queryEvaluator.select("SELECT 1 FROM DUAL") { r => r.getInt(1) } mustEqual List(1)
      queryEvaluator.select("SELECT 2 FROM DUAL") { r => r.getInt(1) } mustEqual List(2)
    }

    "failfast after a host is down" >> {
      val queryEvaluator = testEvaluatorFactory(config)

      queryEvaluator.select("SELECT 1 FROM DUAL") { r => r.getInt(1) } mustEqual List(1)
      FakeContext.markServerDown(host)
      try {
        queryEvaluator.select("SELECT 1 FROM DUAL") { r => r.getInt(1) } must throwA[CommunicationsException]
        val t0 = Time.now
        queryEvaluator.select("SELECT 1 FROM DUAL") { r => r.getInt(1) } must throwA[PoolEmptyException]
        (Time.now - t0).inMillis must beCloseTo(0L, 100L)
      } finally {
        FakeContext.markServerUp(host)
      }
    }

    "failfast after connections are closed due to query timeout" >> {
      val queryEvaluator = testEvaluatorFactory(config)

      queryEvaluator.select("SELECT 1 FROM DUAL") { r => r.getInt(1) } mustEqual List(1)
      FakeContext.setTimeTakenToExecQuery(host, 1.second)
      try {
        // this will cause the underlying connection being destroyed
        queryEvaluator.select("SELECT 1 FROM DUAL") { r => r.getInt(1) } must throwA[SqlQueryTimeoutException]
        val t0 = Time.now
        queryEvaluator.select("SELECT 1 FROM DUAL") { r => r.getInt(1) } must throwA[PoolEmptyException]
        (Time.now - t0).inMillis must beCloseTo(0L, 100L)
      } finally {
        FakeContext.setTimeTakenToExecQuery(host, 0.second)
      }
    }
  }

  doAfterSpec { Database.driverName = "jdbc:mysql" }
}
