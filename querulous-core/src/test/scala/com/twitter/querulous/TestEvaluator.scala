package com.twitter.querulous

import com.twitter.querulous.database.SingleConnectionDatabaseFactory
import com.twitter.querulous.query.SqlQueryFactory
import com.twitter.querulous.evaluator.{QueryEvaluator, StandardQueryEvaluatorFactory}
import com.twitter.util.Eval
import java.io.{File, FileNotFoundException}
import java.util.concurrent.CountDownLatch
import org.specs.Specification

import config.Connection

trait ConfiguredSpecification extends Specification {
  lazy val config = try {
    val eval = new Eval
    val configFile =
      Some(new File("querulous/config/test.scala")) filter { _.exists } orElse {
        Some(new File("config/test.scala")) filter { _.exists }
      } getOrElse {
        throw new FileNotFoundException("config/test.scala")
      }
    eval[Connection](configFile)
  } catch {
    case e =>
      e.printStackTrace()
      throw e
  }

  def skipIfCI(f: => Unit) {
    if (System.getenv().containsKey("SBT_CI")) {
      skip("skipping on CI machine")
    } else {
      f
    }
  }
}

object TestEvaluator {
//  val testDatabaseFactory = new MemoizingDatabaseFactory()
  val testDatabaseFactory = new SingleConnectionDatabaseFactory
  val testQueryFactory = new SqlQueryFactory
  val testEvaluatorFactory = new StandardQueryEvaluatorFactory(testDatabaseFactory, testQueryFactory)

  private val userEnv = System.getenv("DB_USERNAME")
  private val passEnv = System.getenv("DB_PASSWORD")

  def getDbLock(queryEvaluator: QueryEvaluator, lockName: String) = {
    val returnLatch = new CountDownLatch(1)
    val releaseLatch = new CountDownLatch(1)

    val thread = new Thread() {
      override def run() {
        queryEvaluator.select("SELECT GET_LOCK('" + lockName + "', 1) AS rv") { row =>
          returnLatch.countDown()
          try {
            releaseLatch.await()
          } catch {
            case _ =>
          }
        }
      }
    }

    thread.start()
    returnLatch.await()

    releaseLatch
  }
}
