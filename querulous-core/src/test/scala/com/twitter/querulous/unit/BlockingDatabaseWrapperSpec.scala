package com.twitter.querulous.unit

import org.specs.Specification
import java.sql.Connection
import java.util.concurrent.atomic._
import com.twitter.util.Future
import com.twitter.querulous.database._
import com.twitter.querulous.query._
import com.twitter.querulous.async._
import com.twitter.conversions.time._


class BlockingDatabaseWrapperSpec extends Specification {
  "BlockingDatabaseWrapper" should {
    val database = new DatabaseProxy {
      var database: Database = null // don't care
      val totalOpens         = new AtomicInteger(0)
      val openConns          = new AtomicInteger(0)

      // override the methods BlockingDatabaseWrapper uses.
      override def openTimeout = 500.millis
      override def hosts = List("localhost")

      def open() = {
        totalOpens.incrementAndGet
        openConns.incrementAndGet
        null.asInstanceOf[Connection]
      }

      def close(c: Connection) { openConns.decrementAndGet }

      def reset() { totalOpens.set(0); openConns.set(0) }
    }

    val numThreads = 2
    val wrapper = new BlockingDatabaseWrapper(numThreads, database)

    doBefore { database.reset() }

    "withConnection should follow connection lifecycle" in {
      wrapper withConnection { _ => "Done" } apply()

      database.totalOpens.get mustEqual 1
      database.openConns.get  mustEqual 1
    }

    "withConnection should return connection on exception" in {
      wrapper withConnection { _ => throw new Exception } handle { case _ => "Done with Exception" } apply()

      database.totalOpens.get mustEqual 1
      database.openConns.get  mustEqual 0
    }

    "withConnection should not be interrupted if already executing" in {
      val result = wrapper withConnection { _ =>
        Thread.sleep(1000)
        "Done"
      } apply()

      result mustBe "Done"
      database.totalOpens.get mustEqual 1
      database.openConns.get  mustEqual 1
    }

    "withConnection should follow lifecycle regardless of cancellation" in {
      val hitBlock = new AtomicInteger(0)
      val futures = for (i <- 1 to 100000) yield {
        val f = wrapper.withConnection { _ =>
          hitBlock.incrementAndGet
          "Done"
        } handle {
          case e => "Cancelled"
        }

        f.cancel()
        f
      }

      val results = Future.collect(futures).apply()
      val completed = results partition { _ == "Done" } _1


      // println debugging
      println("Opened:    "+ database.totalOpens.get)
      println("Ran block: "+ hitBlock.get)
      println("Cancelled: "+ (100000 - completed.size))
      println("Completed: "+ completed.size)
      println("Cached:    "+ database.openConns.get)

      database.totalOpens.get mustEqual numThreads
      database.openConns.get mustEqual numThreads
    }
  }
}
