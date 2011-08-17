package com.twitter.querulous.unit

import com.twitter.conversions.time._
import org.specs.Specification
import org.specs.mock.{ClassMocker, JMocker}
import com.twitter.util.Duration
import java.sql.Connection
import java.security.InvalidParameterException
import com.twitter.querulous.database.{FailFastBasedOnNumConnsPolicy, PoolFailFastException, PoolEmptyException, ThrottledPool}

trait TestConnectionFactory {
  def getConnection(timeout: Duration): Connection
}

class FailFastBasedOnNumConnsPolicySpec extends Specification with JMocker with ClassMocker {
  val conn = mock[Connection]
  val rng = mock[scala.util.Random]
  val connFactory = mock[TestConnectionFactory]
  val ffp = FailFastBasedOnNumConnsPolicy(0.75, 0.5, 1.second, Some(rng))
  val pool = mock[ThrottledPool]

  "FailFastBasedOnNumConnsPolicySpec" should {
    "throw InvalidParameterException when highWaterMark is lower than lowWaterMark" in {
      FailFastBasedOnNumConnsPolicy(0.5, 0.75, 1.second, Some(rng)) must throwA[InvalidParameterException]
    }

    "throw InvalidParameterException when lowWaterMark is negative or 0" in {
      FailFastBasedOnNumConnsPolicy(0.5, -0.1, 1.second, Some(rng)) must throwA[InvalidParameterException]
    }

    "throw InvalidParameterException when hightWaterMark is greater than 1" in {
      FailFastBasedOnNumConnsPolicy(1.1, 0.75, 1.second, Some(rng)) must throwA[InvalidParameterException]
    }

    "throw PoolEmptyException when the pool does not have any connection" in {
      expect {
        one(pool).getTotal() willReturn 0
      }
      ffp.failFast(pool)(connFactory.getConnection(_)) must throwA[PoolEmptyException]
    }

    "throw PoolFailFastException when pool below lowWaterMark and unlucky" in {
      expect {
        one(pool).getTotal() willReturn 2
        one(pool).size willReturn 8
        one(rng).nextDouble() willReturn 0.75
        one(pool).size willReturn 8
      }
      ffp.failFast(pool)(connFactory.getConnection(_)) must throwA[PoolFailFastException]
    }

    "get a connection with the more aggressive timeout setting when pool below lowWaterMark and but lucky enough" in {
      expect {
        one(pool).getTotal() willReturn 2
        one(pool).size willReturn 8
        one(rng).nextDouble() willReturn 0.25
        one(pool).size willReturn 8
        one(connFactory).getConnection(1.second) willReturn conn
      }
      ffp.failFast(pool)(connFactory.getConnection(_)) mustEqual conn
    }

    "get a connection with the more aggressive timeout setting when pool below highWaterMark but at lowWaterMark" in {
      expect {
        one(pool).getTotal() willReturn 4
        2.of(pool).size willReturn 8
        one(connFactory).getConnection(1.second) willReturn conn
      }
      ffp.failFast(pool)(connFactory.getConnection(_)) mustEqual conn
    }

    "get a connection with the more aggressive timeout setting when pool below highWaterMark but above lowWaterMark" in {
      expect {
        one(pool).getTotal() willReturn 5
        2.of(pool).size willReturn 8
        one(connFactory).getConnection(1.second) willReturn conn
      }
      ffp.failFast(pool)(connFactory.getConnection(_)) mustEqual conn
    }

    "get a connection with the normal timeout setting when pool at highWaterMark" in {
      expect {
        one(pool).getTotal() willReturn 6
        2.of(pool).size willReturn 8
        one(pool).timeout willReturn 2.seconds
        one(connFactory).getConnection(2.seconds) willReturn conn
      }
      ffp.failFast(pool)(connFactory.getConnection(_)) mustEqual conn
    }

    "get a connection with the normal timeout setting when pool above highWaterMark" in {
      expect {
        one(pool).getTotal() willReturn 7
        2.of(pool).size willReturn 8
        one(pool).timeout willReturn 2.seconds
        one(connFactory).getConnection(2.seconds) willReturn conn
      }
      ffp.failFast(pool)(connFactory.getConnection(_)) mustEqual conn
    }
  }
}
