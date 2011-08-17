package com.twitter.querulous.database

import java.util.concurrent.{TimeUnit, LinkedBlockingQueue}
import java.sql.{SQLException, DriverManager, Connection}
import org.apache.commons.dbcp.{PoolingDataSource, DelegatingConnection}
import org.apache.commons.pool.{PoolableObjectFactory, ObjectPool}
import com.twitter.querulous.config.FailFastPolicyConfig
import com.twitter.util.Duration
import com.twitter.util.Time
import scala.annotation.tailrec
import java.util.concurrent.atomic.AtomicInteger
import java.security.InvalidParameterException
import util.Random
import java.lang.{UnsupportedOperationException, Thread}

class FailToAcquireConnectionException extends SQLException
class PoolTimeoutException extends FailToAcquireConnectionException
class PoolFailFastException extends FailToAcquireConnectionException
class PoolEmptyException extends PoolFailFastException

/**
 * determine whether to fail fast when trying to check out a connection from pool
 */
trait FailFastPolicy {
  /**
   * This method throws PoolFailFastException when it decides to fail fast given the current state
   * of the underlying database, or it could throw PoolTimeoutException when failing to acquire
   * a connection within specified time frame
   *
   * @param db the database from which we are going to open connections
   */
  @throws(classOf[FailToAcquireConnectionException])
  def failFast(pool: ObjectPool)(f: Duration => Connection): Connection
}

/**
 * This policy behaves in the way specified as follows:
 * When the number of connections in the pool is below the highWaterMark, start to use the timeout
 * passed in when waiting for a connection; when it is below the lowWaterMark, start to fail
 * immediately proportional to the number of connections available in the pool, with 100% failure
 * rate when the pool is empty
 */
class FailFastBasedOnNumConnsPolicy(val highWaterMark: Double,  val lowWaterMark: Double,
  val openTimeout: Duration, val rng: Random) extends FailFastPolicy {
  if (highWaterMark < lowWaterMark || highWaterMark > 1 || lowWaterMark < 0) {
    throw new InvalidParameterException("invalid water mark")
  }

  @throws(classOf[FailToAcquireConnectionException])
  def failFast(pool: ObjectPool)(f: Duration => Connection) = {
    pool match {
      case p: ThrottledPool => {
        val numConn = p.getTotal()
        if (numConn == 0) {
          throw new PoolEmptyException
        } else if (numConn < p.size * lowWaterMark) {
          if(numConn < rng.nextDouble() * p.size * lowWaterMark) {
            throw new PoolFailFastException
          } else {
            // should still try to do aggressive timeout at least
            f(openTimeout)
          }
        } else if (numConn < p.size * highWaterMark) {
          f(openTimeout)
        } else {
          f(p.timeout)
        }
      }
      case _ => throw new UnsupportedOperationException("Only support ThrottledPoolingDatabase")
    }
  }
}

object FailFastBasedOnNumConnsPolicy {
  def apply(openTimeout: Duration): FailFastBasedOnNumConnsPolicy = {
    apply(0, 0, openTimeout, Some(new Random(System.currentTimeMillis())))
  }

  def apply(highWaterMark: Double, lowWaterMark: Double, openTimeout: Duration,
    rng: Option[Random]): FailFastBasedOnNumConnsPolicy = {
    new FailFastBasedOnNumConnsPolicy(highWaterMark, lowWaterMark, openTimeout,
      rng.getOrElse(new Random(System.currentTimeMillis())))
  }
}

class PooledConnection(c: Connection, p: ObjectPool) extends DelegatingConnection(c) {
  private var pool: Option[ObjectPool] = Some(p)

  private def invalidateConnection() {
    pool.foreach { _.invalidateObject(this) }
    pool = None
  }

  override def close() = synchronized {
    val isClosed = try { c.isClosed() } catch {
      case e: Exception => {
        invalidateConnection()
        throw e
      }
    }

    if (!isClosed) {
      pool match {
        case Some(pl) => pl.returnObject(this)
        case None =>
          passivate()
          c.close()
      }
    } else {
      invalidateConnection()
      throw new SQLException("Already closed.")
    }
  }
}

case class ThrottledPool(factory: () => Connection, val size: Int, timeout: Duration,
  idleTimeout: Duration, failFastPolicy: FailFastPolicy) extends ObjectPool {
  private val pool = new LinkedBlockingQueue[(Connection, Time)]()
  private val currentSize = new AtomicInteger(0)
  private val numWaiters = new AtomicInteger(0)

  def this(factory: () => Connection, size: Int, timeout: Duration, idleTimeout: Duration) = {
    this(factory, size, timeout, idleTimeout, FailFastBasedOnNumConnsPolicy(timeout))
  }

  for (i <- (0.until(size))) addObject()

  def addObject() {
    pool.offer((new PooledConnection(factory(), this), Time.now))
    currentSize.incrementAndGet()
  }

  def addObjectIfEmpty() = synchronized {
    if (getTotal() == 0) addObject()
  }

  def addObjectUnlessFull() = synchronized {
    if (getTotal() < size) {
      addObject()
    }
  }

  final def borrowObject(): Connection = {
    numWaiters.incrementAndGet()
    try {
      failFastPolicy.failFast(this)(borrowObjectInternal)
    } finally {
      numWaiters.decrementAndGet()
    }
  }

  @tailrec private def borrowObjectInternal(openTimeout: Duration): Connection = {
    // short circuit if the pool is empty
    if (getTotal() == 0) throw new PoolEmptyException

    val pair = pool.poll(openTimeout.inMillis, TimeUnit.MILLISECONDS)
    if (pair == null) throw new PoolTimeoutException
    val (connection, lastUse) = pair

    if ((Time.now - lastUse) > idleTimeout) {
      // TODO: perhaps replace with forcible termination.
      try { connection.close() } catch { case _: SQLException => }
      // note: dbcp handles object invalidation here.
      addObjectIfEmpty()
      borrowObjectInternal(openTimeout)
    } else {
      connection
    }
  }

  def clear() {
    pool.clear()
  }

  def close() {
    pool.clear()
  }

  def getNumActive(): Int = {
    currentSize.get() - pool.size()
  }

  def getNumIdle(): Int = {
    pool.size()
  }

  def getTotal() = {
    currentSize.get()
  }

  def getNumWaiters() = {
    numWaiters.get()
  }

  def invalidateObject(obj: Object) {
    currentSize.decrementAndGet()
  }

  def returnObject(obj: Object) {
    val conn = obj.asInstanceOf[Connection]

    pool.offer((conn, Time.now))
  }

  def setFactory(factory: PoolableObjectFactory) {
    // deprecated
  }
}

class PoolWatchdogThread(
  pool: ThrottledPool,
  hosts: Seq[String],
  repopulateInterval: Duration) extends Thread(hosts.mkString(",") + "-pool-watchdog") {

  this.setDaemon(true)

  override def run() {
    var lastTimePoolPopulated = Time.now
    while(true) {
      try {
        val timeToSleepInMills = (repopulateInterval - (Time.now - lastTimePoolPopulated)).inMillis
        if (timeToSleepInMills > 0) {
          Thread.sleep(timeToSleepInMills)
        }
        lastTimePoolPopulated = Time.now
        pool.addObjectUnlessFull()
      } catch {
        case t: Throwable => {
          System.err.println(Time.now.format("yyyy-MM-dd HH:mm:ss Z") + ": " +
                             Thread.currentThread().getName() +
                             " failed to add connection to the pool")
          t.printStackTrace(System.err)
        }
      }
    }
  }

  // TODO: provide a reliable way to have this thread exit when shutdown is implemented
}

class ThrottledPoolingDatabaseFactory(
  serviceName: Option[String],
  size: Int,
  openTimeout: Duration,
  idleTimeout: Duration,
  repopulateInterval: Duration,
  defaultUrlOptions: Map[String, String],
  failFastPolicyConfig: Option[FailFastPolicyConfig]) extends DatabaseFactory {

  // the default is the one with both highWaterMark and lowWaterMark of 0
  // in this case, PoolEmptyException will be thrown when the number of connections in the pool
  // is zero; otherwise, it will behave the same way as if this policy is not applied
  private val failFastPolicy = failFastPolicyConfig map {pc =>
    FailFastBasedOnNumConnsPolicy(pc.highWaterMark, pc.lowWaterMark, pc.openTimeout, pc.rng)
  } getOrElse (FailFastBasedOnNumConnsPolicy(openTimeout))

  def this(size: Int, openTimeout: Duration, idleTimeout: Duration, repopulateInterval: Duration,
    defaultUrlOptions: Map[String, String]) = {
    this(None, size, openTimeout, idleTimeout, repopulateInterval, defaultUrlOptions, None)
  }

  def this(size: Int, openTimeout: Duration, idleTimeout: Duration,
    repopulateInterval: Duration) = {
    this(size, openTimeout, idleTimeout, repopulateInterval, Map.empty)
  }

  def apply(dbhosts: List[String], dbname: String, username: String, password: String,
    urlOptions: Map[String, String]) = {
    val finalUrlOptions =
      if (urlOptions eq null) {
      defaultUrlOptions
    } else {
      defaultUrlOptions ++ urlOptions
    }

    new ThrottledPoolingDatabase(serviceName, dbhosts, dbname, username, password, finalUrlOptions,
      size, openTimeout, idleTimeout, repopulateInterval, failFastPolicy)
  }
}

class ThrottledPoolingDatabase(
  val serviceName: Option[String],
  val hosts: List[String],
  val name: String,
  val username: String,
  password: String,
  val extraUrlOptions: Map[String, String],
  numConnections: Int,
  val openTimeout: Duration,
  idleTimeout: Duration,
  repopulateInterval: Duration,
  val failFastPolicy: FailFastPolicy) extends Database {

  Class.forName("com.mysql.jdbc.Driver")

  private[database] val pool = new ThrottledPool(mkConnection, numConnections, openTimeout,
    idleTimeout, failFastPolicy)
  private val poolingDataSource = new PoolingDataSource(pool)
  poolingDataSource.setAccessToUnderlyingConnectionAllowed(true)
  new PoolWatchdogThread(pool, hosts, repopulateInterval).start()
  private val gaugePrefix = serviceName.map{ _ + "-" }.getOrElse("")

  private val gauges = List(
    (gaugePrefix + hosts.mkString(",") + "-num-connections", () => {pool.getTotal().toDouble}),
    (gaugePrefix + hosts.mkString(",") + "-num-idle-connections", () => {pool.getNumIdle().toDouble}),
    (gaugePrefix + hosts.mkString(",") + "-num-waiters", () => {pool.getNumWaiters().toDouble})
  )

  def this(hosts: List[String], name: String, username: String, password: String,
    extraUrlOptions: Map[String, String], numConnections: Int, openTimeout: Duration,
    idleTimeout: Duration, repopulateInterval: Duration) = {
    this(None, hosts, name, username, password, extraUrlOptions, numConnections, openTimeout,
      idleTimeout, repopulateInterval, FailFastBasedOnNumConnsPolicy(openTimeout))
  }

  def open() = {
    try {
      poolingDataSource.getConnection()
    } catch {
      case e: PoolTimeoutException =>
        throw new SqlDatabaseTimeoutException(hosts.mkString(",") + "/" + name, openTimeout)
    }
  }

  def close(connection: Connection) {
    try { connection.close() } catch { case _: SQLException => }
  }

  protected def mkConnection(): Connection = {
    DriverManager.getConnection(url(hosts, name, urlOptions), username, password)
  }

  override protected[database] def getGauges: Seq[(String, ()=>Double)] = {
    return gauges;
  }
}
