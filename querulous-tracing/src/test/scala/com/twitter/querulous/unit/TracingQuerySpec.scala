package com.twitter.querulous.unit

import java.sql.Connection
import org.specs.Specification
import org.specs.mock.JMocker
import com.twitter.querulous.query._
import com.twitter.finagle.tracing._

class TracingQuerySpec extends Specification with JMocker {
  "TracingQuery" should {

    "add records as query is executed" in {
      val queryString = "select * from users"
      val tracer = mock[Tracer]
      val connection = mock[Connection]
      Trace.pushId(TraceId(Some(SpanId(1)), None, SpanId(1), Some(true)))

      expect {
        one(connection).getClientInfo("ClientHostname")
        one(connection).prepareStatement("select * from users /*~ {\"trace_id\":\"0000000000000001\"}*/")
        exactly(5).of(tracer).record(a[Record])
      }

      val query = new SqlQuery(connection, queryString)
      val tracingQuery = new TracingQuery(query, connection, QueryClass.Select,
        "service", tracer, true)
      tracingQuery.execute()
    }
  }
}
