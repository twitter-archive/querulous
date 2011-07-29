package com.twitter.querulous.test.sql

import java.sql._
import java.util.Properties
import com.mysql.jdbc.exceptions.jdbc4.CommunicationsException
import java.net.SocketException

class FakeConnection(val url: String, val info: Properties) extends Connection {
  private[this] var open: Boolean = false
  private[this] var autoCommit: Boolean = false
  private[this] var readOnly: Boolean = false
  private[this] var catalog: String = ""
  private[this] var transIsoLevel: Int = Connection.TRANSACTION_REPEATABLE_READ;
  private[sql] val host: String = FakeConnection.host(url)
  private[this] val properties: Properties = FakeConnection.properties(url, info)
  private[this] var forceClosed: Boolean = false

  FakeConnection.isDown(host)

  open = true

  FakeConnection.checkTimeout(host, properties)

  @throws(classOf[SQLException])
  def createStatement: Statement = {
    checkOpen()
    new FakeStatement(this);
  }

  @throws(classOf[SQLException])
  def prepareStatement(sql: String): PreparedStatement = {
    checkOpen()
    new FakePreparedStatement(this, sql)
  }

  @throws(classOf[SQLException])
  def prepareCall(sql: String): CallableStatement = {
    checkOpen()
    null
  }

  @throws(classOf[SQLException])
  def nativeSQL(sql: String): String = {
    checkOpen()
    ""
  }

  @throws(classOf[SQLException])
  def setAutoCommit(autoCommit: Boolean) {
    checkOpen()
    this.autoCommit = autoCommit
  }

  @throws(classOf[SQLException])
  def getAutoCommit: Boolean = {
    checkOpen()
    this.autoCommit
  }

  @throws(classOf[SQLException])
  def commit() {
    checkOpen()
    if (this.isReadOnly) {
      throw new SQLException("Cannot commit a readonly connection")
    }
  }

  @throws(classOf[SQLException])
  def rollback() {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def close() {
    this.open = false
  }

  def isClosed: Boolean = {
    !this.open
  }

  @throws(classOf[SQLException])
  def getMetaData: DatabaseMetaData = {
    throw new SQLException("Not implemented");
  }

  @throws(classOf[SQLException])
  def setReadOnly(readOnly: Boolean) {
    checkOpen()
    this.readOnly = readOnly
  }

  @throws(classOf[SQLException])
  def isReadOnly: Boolean = {
    checkOpen()
    this.readOnly
  }

  @throws(classOf[SQLException])
  def setCatalog(catalog: String) {
    checkOpen()
    this.catalog = catalog
  }

  @throws(classOf[SQLException])
  def getCatalog: String = {
    checkOpen()
    this.catalog
  }

  @throws(classOf[SQLException])
  def setTransactionIsolation(level: Int) {
    checkOpen()
    this.transIsoLevel = level
  }

  @throws(classOf[SQLException])
  def getTransactionIsolation: Int = {
    checkOpen()
    this.transIsoLevel
  }

  @throws(classOf[SQLException])
  def getWarnings: SQLWarning = {
    checkOpen()
    null
  }

  @throws(classOf[SQLException])
  def clearWarnings() {
    checkOpen()
  }

  @throws(classOf[SQLException])
  def createStatement(resultSetType: Int, resultSetConcurrency: Int): Statement = {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def prepareStatement(sql: String, resultSetType: Int,
    resultSetConcurrency: Int): PreparedStatement = {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def prepareCall(sql: String, resultSetType: Int, resultSetConcurrency: Int): CallableStatement = {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def getTypeMap: java.util.Map[String, Class[_]] = {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def setTypeMap(map: java.util.Map[String, Class[_]]) {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def setHoldability(holdability: Int) {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def getHoldability: Int = {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def setSavepoint(): Savepoint = {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def setSavepoint(name: String): Savepoint = {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def rollback(savepoint: Savepoint) {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def releaseSavepoint(savepoint: Savepoint) {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def createStatement(resultSetType: Int, resultSetConcurrency: Int,
    resultSetHoldability: Int): Statement = {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def prepareStatement(sql: String, resultSetType: Int, resultSetConcurrency: Int,
    resultSetHoldability: Int): PreparedStatement = {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def prepareCall(sql: String, resultSetType: Int, resultSetConcurrency: Int,
    resultSetHoldability: Int): CallableStatement = {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def prepareStatement(sql: String, autoGeneratedKeys: Int): PreparedStatement = {
    throw new SQLException("Not implemented")
  }

  def prepareStatement(sql: String, columnIndexes: scala.Array[Int]): PreparedStatement = {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def prepareStatement(sql: String, columnNames: scala.Array[String]): PreparedStatement = {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def createClob: Clob = {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def createBlob: Blob = {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def createNClob: NClob = {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def createSQLXML: SQLXML = {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def isValid(timeout: Int): Boolean = {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def setClientInfo(name: String, value: String) {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def setClientInfo(properties: Properties) {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def getClientInfo(name: String): String = {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def getClientInfo: Properties = {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def createArrayOf(typeName: String, elements: scala.Array[AnyRef]): java.sql.Array = {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def createStruct(typeName: String, attributes: scala.Array[AnyRef]): Struct = {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def isWrapperFor(iface: Class[_]): Boolean = {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def unwrap[T](iface: Class[T]): T = {
    throw new SQLException("Not implemented.");
  }

  @throws(classOf[SQLException])
  private[this] def checkOpen() {
    if (!this.open) {
      throw new SQLException("Connection has been closed")
    }

    try {
      FakeConnection.isDown(host)
    } catch {
      case e: CommunicationsException => {
        this.close()
        throw e
      }
      case e => throw e
    }
  }


  @throws(classOf[SQLException])
  def abortInternal() {
    this.close()
    this.forceClosed = true
  }
}

object FakeConnection {
  def host(url: String): String = {
    if (url == null || url == "") {
      ""
    } else {
      url.indexOf('?') match {
        case -1 => url.substring(FakeDriver.DRIVER_NAME.length + 3)
        case _ => url.substring(FakeDriver.DRIVER_NAME.length + 3, url.indexOf('?'))
      }
    }
  }

  def properties(url: String, info: Properties): Properties = {
    url.indexOf('?') match {
      case -1 => info
      case _ => {
        val newInfo = new Properties(info)
        url.substring(url.indexOf('?') + 1).split("&") foreach {nameVal =>
          nameVal.split("=").toList match {
            case Nil =>
            case n :: Nil =>
            case n :: v :: _ => newInfo.put(n, v)
          }
        }
        newInfo
      }
    }
  }

  @throws(classOf[CommunicationsException])
  def isDown(host: String) {
    if (FakeContext.isServerDown(host)) {
      throw new CommunicationsException(null, 0, 0, new Exception("Communication link failure"))
    }
  }

  @throws(classOf[CommunicationsException])
  def isDown(conn: FakeConnection) {
    if (FakeContext.isServerDown(conn.host)) {
      // real driver mark the connection as closed when running into communication problem too
      conn.close()
      throw new CommunicationsException(null, 0, 0, new Exception("Communication link failure"))
    }
  }

  @throws(classOf[SocketException])
  def checkTimeout(host: String, properties: Properties) {
    val connectTimeoutInMillis: Long = properties match {
      case null => 0L
      case _ => properties.getProperty("connectTimeout", "0").toLong
    }

    val timeTakenToOpenConnInMiills = FakeContext.getTimeTakenToOpenConn(host).inMillis
    if (timeTakenToOpenConnInMiills > connectTimeoutInMillis) {
      Thread.sleep(connectTimeoutInMillis)
      throw new SocketException("Connection timeout")
    } else {
      Thread.sleep(timeTakenToOpenConnInMiills)
    }
  }
}