import com.twitter.querulous.config.Connection
import com.twitter.querulous.database.Database
import com.twitter.querulous.test.sql.FakeDriver

new Connection {
  val hostnames = Seq("fake")
  val database = "fake"
  val username = "fake"
  val password = "fake"
}
