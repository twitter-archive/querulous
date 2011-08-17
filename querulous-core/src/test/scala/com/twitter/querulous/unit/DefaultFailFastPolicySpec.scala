package com.twitter.querulous.unit

import com.twitter.conversions.time._
import org.specs.Specification
import org.specs.mock.{ClassMocker, JMocker}
import java.sql.Connection
import com.twitter.querulous.database.{FailFastBasedOnNumConnsPolicy, PoolEmptyException, ThrottledPool}

class DefaultFailFastPolicySpec extends Specification with JMocker with ClassMocker {
  val conn = mock[Connection]
  val connFactory = mock[TestConnectionFactory]
  val ffp = FailFastBasedOnNumConnsPolicy(0, 0, 1.second, None)
  val pool = mock[ThrottledPool]

  "DefaultFailFastPolicySpec" should {
    "throw PoolEmptyException when the pool does not have any connection" in {
      expect {
        one(pool).getTotal() willReturn 0
      }
      ffp.failFast(pool)(connFactory.getConnection(_)) must throwA[PoolEmptyException]
    }

    "get a connection with the normal timeout setting even if the pool only has one connection" in {
      expect {
        one(pool).getTotal() willReturn 1
        2.of(pool).size willReturn 8
        one(pool).timeout willReturn 2.seconds
        one(connFactory).getConnection(2.seconds) willReturn conn
      }
      ffp.failFast(pool)(connFactory.getConnection(_)) mustEqual conn
    }
  }
}
