package com.twitter.querulous.integration

import com.twitter.conversions.time._
import com.twitter.querulous.evaluator.{QueryEvaluator, StandardQueryEvaluatorFactory}
import com.twitter.querulous.ConfiguredSpecification
import com.twitter.querulous.sql.{FakeContext, FakeDriver}
import com.mysql.jdbc.exceptions.jdbc4.CommunicationsException
import com.twitter.querulous.query.{SqlQueryTimeoutException, TimingOutQueryFactory, SqlQueryFactory}
import collection.immutable.Vector
import util.Random
import com.twitter.util.{Duration, Time}
import com.twitter.querulous.database.{PoolFailFastException, PoolEmptyException, Database, ThrottledPoolingDatabaseFactory}
import com.twitter.querulous.config.FailFastPolicyConfig

class MockRandom extends Random {
  private[this] var currIndex = 0
  private[this] val vals: IndexedSeq[Double] = Vector(0.75, 0.85, 0.25, 0.25, 0.25)
  // cycle through the list of values specified above
  override def nextDouble(): Double = {
    currIndex += 1
    vals((currIndex - 1)%vals.size)
  }
}

object ThrottledPoolingDatabaseWithFakeConnSpec {
  // configure repopulation interval to a minute to avoid conn repopulation when test running
  val testDatabaseFactory = new ThrottledPoolingDatabaseFactory(1, 1.second, 1.second, 60.seconds, Map.empty)
  // configure repopulation interval to 1 second so that we can verify the watchdog actually works
  val testRepopulatedDatabaseFactory = new ThrottledPoolingDatabaseFactory(1, 1.second, 1.second, 1.second, Map.empty)
  val testQueryFactory = new TimingOutQueryFactory(new SqlQueryFactory, 500.millis, true)
  val testEvaluatorFactory = new StandardQueryEvaluatorFactory(testDatabaseFactory, testQueryFactory)
  val testRepopulatedEvaluatorFactory = new StandardQueryEvaluatorFactory(testRepopulatedDatabaseFactory, testQueryFactory)
  // configure repopulation interval to 100ms, and connection timeout to 2 seconds
  val testRepopulatedLongConnTimeoutDbFactory = new ThrottledPoolingDatabaseFactory(1, 1.second,
    1.second, 100.milliseconds, Map("connectTimeout" -> "2000"))
  val testRepopulatedLongConnTimeoutEvaluatorFactory = new StandardQueryEvaluatorFactory(
    testRepopulatedLongConnTimeoutDbFactory, testQueryFactory)

  val failFastPolicyConfig = new FailFastPolicyConfig {
    def highWaterMark: Double = 0.75
    def openTimeout: Duration = 1.second
    def lowWaterMark: Double = 0.5
    def rng: Option[Random] = Some(new MockRandom())
  }
  val testFailFastDatabaseFactory = new ThrottledPoolingDatabaseFactory(Some("test"), 8, 1.second,
    60.second, 60.seconds, Map.empty, Some(failFastPolicyConfig))
  val testFailFastEvaluatorFactory = new StandardQueryEvaluatorFactory(testFailFastDatabaseFactory,
    testQueryFactory)

  val testDatabaseFactoryWithDefaultFailFastPolicy = new ThrottledPoolingDatabaseFactory(
    Some("test"), 8, 1.second, 60.second, 60.seconds, Map.empty, None)
  val testEvaluatorFactoryWithDefaultFailFastPolicy = new StandardQueryEvaluatorFactory(
    testDatabaseFactoryWithDefaultFailFastPolicy, testQueryFactory)

  def destroyConnection(queryEvaluator: QueryEvaluator, host: String, numConns: Int = 1) {
    FakeContext.markServerDown(host)
    try {
      for(i <- 0 until numConns) {
        try {
          queryEvaluator.select("SELECT 1 FROM DUAL") { r => r.getInt(1) }
          assert(false)
        } catch {
          case e: CommunicationsException => // expected
          case t: Throwable => throw t
        }
      }
    } finally {
      FakeContext.markServerUp(host)
    }
  }
}

class ThrottledPoolingDatabaseWithFakeConnSpec extends ConfiguredSpecification {
  import ThrottledPoolingDatabaseWithFakeConnSpec._

  val host = config.hostnames.mkString(",") + "/" + config.database
  FakeContext.setQueryResult(host, "SELECT 1 FROM DUAL", Array(Array[java.lang.Object](1.asInstanceOf[AnyRef])))
  FakeContext.setQueryResult(host, "SELECT 2 FROM DUAL", Array(Array[java.lang.Object](2.asInstanceOf[AnyRef])))

  doBeforeSpec { Database.driverName = FakeDriver.DRIVER_NAME }

  "ThrottledJdbcPoolSpec" should {
    val queryEvaluator = testEvaluatorFactory(config)
    "execute some queries" in {
      queryEvaluator.select("SELECT 1 FROM DUAL") { r => r.getInt(1) } mustEqual List(1)
      queryEvaluator.select("SELECT 2 FROM DUAL") { r => r.getInt(1) } mustEqual List(2)
    }

    "failfast after a host is down" in {
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

    "failfast after connections are closed due to query timeout" in {
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

    "repopulate the pool every repopulation interval" in {
      val queryEvaluator = testRepopulatedEvaluatorFactory(config)

      queryEvaluator.select("SELECT 1 FROM DUAL") { r => r.getInt(1) } mustEqual List(1)
      FakeContext.setTimeTakenToExecQuery(host, 1.second)
      try {
        // this will cause the underlying connection being destroyed
        queryEvaluator.select("SELECT 1 FROM DUAL") { r => r.getInt(1) } must throwA[SqlQueryTimeoutException]
        Thread.sleep(2000)
        FakeContext.setTimeTakenToExecQuery(host, 0.second)
        // after repopulation, biz as usual
        queryEvaluator.select("SELECT 1 FROM DUAL") { r => r.getInt(1) } mustEqual  List(1)
      } finally {
        FakeContext.setTimeTakenToExecQuery(host, 0.second)
      }
    }

    "repopulate the pool even if it takes longer to establish a connection than repopulation interval" in {
      val queryEvaluator = testRepopulatedLongConnTimeoutEvaluatorFactory(config)

      queryEvaluator.select("SELECT 1 FROM DUAL") { r => r.getInt(1) } mustEqual List(1)
      FakeContext.markServerDown(host)
      try {
        // this will cause the underlying connection being destroyed
        queryEvaluator.select("SELECT 1 FROM DUAL") { r => r.getInt(1) } must throwA[CommunicationsException]
        FakeContext.setTimeTakenToOpenConn(host, 1.second)
        FakeContext.markServerUp(host)
        Thread.sleep(2000)
        // after repopulation, biz as usual
        queryEvaluator.select("SELECT 1 FROM DUAL") { r => r.getInt(1) } mustEqual  List(1)
      } finally {
        FakeContext.setTimeTakenToOpenConn(host, 0.second)
        FakeContext.markServerUp(host)
      }
    }
  }

  "ThrottledJdbcPoolWithFailFastPolicy" should {
    val queryEvaluator = testFailFastEvaluatorFactory(config)

    "execute query normally as the pool is full or above highWaterMark" in {
      // number of connections in the pool 8
      queryEvaluator.select("SELECT 1 FROM DUAL") { r => r.getInt(1) } mustEqual List(1)
      destroyConnection(queryEvaluator, host)
      // number of connections in the pool 7
      queryEvaluator.select("SELECT 1 FROM DUAL") { r => r.getInt(1) } mustEqual List(1)
    }

    // here we should see more aggressive timeout applied on acquiring connection, but currently
    // it is hard to test that. Nonetheless, this is covered by the unit test.
    "execute query normally until the pool reaches the lowWaterMark" in {
      destroyConnection(queryEvaluator, host, 2)
      // number of connections in the pool 6 = 8 * 0.75, at highWaterMark
      queryEvaluator.select("SELECT 1 FROM DUAL") { r => r.getInt(1) } mustEqual List(1)
      destroyConnection(queryEvaluator, host)
      queryEvaluator.select("SELECT 1 FROM DUAL") { r => r.getInt(1) } mustEqual List(1)
      destroyConnection(queryEvaluator, host)
      // number of connection is 4 = 8 * 0.5, at lowWaterMark
      queryEvaluator.select("SELECT 1 FROM DUAL") { r => r.getInt(1) } mustEqual List(1)
    }

    "fail fast when the pool is under the lowWaterMark and throw PoolEmptyException when the pool is empty" in {
      destroyConnection(queryEvaluator, host, 5)
      // number of connection is at 3, but still fine because the first double out of random number
      // generator is 0.75, 3 = 8 * 0.5 * 0.75
      queryEvaluator.select("SELECT 1 FROM DUAL") { r => r.getInt(1) } mustEqual List(1)
      // 3 < 8 * 0.5 * 0.5
      queryEvaluator.select("SELECT 1 FROM DUAL") { r => r.getInt(1) } must throwA[PoolFailFastException]
      destroyConnection(queryEvaluator, host, 3)
      queryEvaluator.select("SELECT 1 FROM DUAL") { r => r.getInt(1) } must throwA[PoolEmptyException]
    }
  }

  "ThrottledJdbcPoolWithDefaultFailFastPolicy" should {
    "execute query normally as the pool until there is no connection in the pool" in {
      val queryEvaluator = testEvaluatorFactoryWithDefaultFailFastPolicy(config)

      // number of connections in the pool 8
      queryEvaluator.select("SELECT 1 FROM DUAL") { r => r.getInt(1) } mustEqual List(1)
      destroyConnection(queryEvaluator, host)
      // number of connections in the pool 7
      queryEvaluator.select("SELECT 1 FROM DUAL") { r => r.getInt(1) } mustEqual List(1)
      destroyConnection(queryEvaluator, host)
      // number of connections in the pool 6
      queryEvaluator.select("SELECT 1 FROM DUAL") { r => r.getInt(1) } mustEqual List(1)
      destroyConnection(queryEvaluator, host)
      // number of connections in the pool 5
      queryEvaluator.select("SELECT 1 FROM DUAL") { r => r.getInt(1) } mustEqual List(1)
      destroyConnection(queryEvaluator, host)
      // number of connections in the pool 4
      queryEvaluator.select("SELECT 1 FROM DUAL") { r => r.getInt(1) } mustEqual List(1)
      destroyConnection(queryEvaluator, host)
      // number of connections in the pool 3
      queryEvaluator.select("SELECT 1 FROM DUAL") { r => r.getInt(1) } mustEqual List(1)
      destroyConnection(queryEvaluator, host)
      // number of connections in the pool 2
      queryEvaluator.select("SELECT 1 FROM DUAL") { r => r.getInt(1) } mustEqual List(1)
      destroyConnection(queryEvaluator, host)
      // number of connections in the pool 1
      queryEvaluator.select("SELECT 1 FROM DUAL") { r => r.getInt(1) } mustEqual List(1)
      destroyConnection(queryEvaluator, host)
      // no connection left in the pool
      queryEvaluator.select("SELECT 1 FROM DUAL") { r => r.getInt(1) } must throwA[PoolEmptyException]
    }
  }

  doAfterSpec { Database.driverName = "jdbc:mysql" }
}
