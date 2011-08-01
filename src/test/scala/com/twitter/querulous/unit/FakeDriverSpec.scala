package com.twitter.querulous.unit

import com.twitter.querulous.ConfiguredSpecification
import com.twitter.util.TimeConversions._
import com.twitter.querulous.sql.FakeDriver
import org.apache.commons.dbcp.DelegatingConnection
import com.twitter.querulous.database._

class FakeDriverSpec extends ConfiguredSpecification {
  doBeforeSpec { Database.driverName = FakeDriver.DRIVER_NAME }

  def testFactory(factory: DatabaseFactory) {
    "the real connection should be FakeConnection" in {
      val db    = factory(config.hostnames.toList, null, config.username, config.password)
      val conn = db.open() match {
        case c: DelegatingConnection => c.getInnermostDelegate
        case c => c
      }

      conn.getClass.getSimpleName mustEqual "FakeConnection"
    }
  }

  "SingleConnectionDatabaseFactory" should {
    val factory = new SingleConnectionDatabaseFactory(Map.empty)
    testFactory(factory)
  }

  "ApachePoolingDatabaseFactory" should {
    val factory = new ApachePoolingDatabaseFactory(10, 10, 1.second, 10.millis, false, 0.seconds,
      Map.empty)

    testFactory(factory)
  }

  "ThrottledPoolingDatabaseFactory" should {
    val factory = new ThrottledPoolingDatabaseFactory(10, 1.second, 10.millis, 1.seconds, Map.empty)

    testFactory(factory)
  }

  doAfterSpec { Database.driverName = "jdbc:mysql" }
}
