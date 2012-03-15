package com.twitter.querulous.unit

import org.specs.mock.JMocker
import org.specs.Specification
import com.twitter.querulous.async.{AsyncDatabase, AsyncDatabaseFactory, AsyncMemoizingDatabaseFactory}
import com.twitter.querulous.database.Database

class AsyncMemoizingDatabaseFactorySpec extends Specification with JMocker {
  val username = "username"
  val password = "password"
  val hosts = List("foo")

  "AsyncMemoizingDatabaseFactory" should {
    "apply" in {
      val database1 = mock[AsyncDatabase]
      val database2 = mock[AsyncDatabase]
      val databaseFactory = mock[AsyncDatabaseFactory]
      val memoizingDatabase = new AsyncMemoizingDatabaseFactory(databaseFactory)

      expect {
        one(databaseFactory).apply(hosts, "bar", username, password, Map.empty, Database.DEFAULT_DRIVER_NAME) willReturn database1
        one(databaseFactory).apply(hosts, "baz", username, password, Map.empty, Database.DEFAULT_DRIVER_NAME) willReturn database2
      }
      memoizingDatabase(hosts, "bar", username, password) mustBe database1
      memoizingDatabase(hosts, "bar", username, password) mustBe database1
      memoizingDatabase(hosts, "baz", username, password) mustBe database2
      memoizingDatabase(hosts, "baz", username, password) mustBe database2
    }

    "not cache" in {
      val database = mock[AsyncDatabase]
      val factory = mock[AsyncDatabaseFactory]
      val memoizingDatabase = new AsyncMemoizingDatabaseFactory(factory)

      expect {
        exactly(2).of(factory).apply(hosts, username, password) willReturn database
      }

      memoizingDatabase(hosts, username, password) mustBe database
      memoizingDatabase(hosts, username, password) mustBe database
    }
  }
}
