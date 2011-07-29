package com.twitter.querulous.test.sql

import java.sql._
import java.util.Calendar
import java.io.{InputStream, Reader}

class FakePreparedStatement(
  val conn: Connection,
  val sql: String,
  var resultSetType: Int,
  var resultSetConcurrency: Int
  ) extends FakeStatement(conn, resultSetType, resultSetConcurrency) with PreparedStatement {

  private[this] var resultSet: ResultSet = null

  def this(conn: Connection, sql: String) = {
    this(conn, sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)
  }

  def getCatalog: String = {
    ""
  }

  @throws(classOf[SQLException])
  override def executeQuery(sqlQuery: String): ResultSet = {
    checkOpen()
    resultSet = new FakeResultSet(this, getFakeResult(sqlQuery), resultSetType, resultSetConcurrency)
    resultSet
  }


  @throws(classOf[SQLException])
  override def executeUpdate(sql: String): Int = {
    checkOpen()
    1
  }

  @throws(classOf[SQLException])
  def executeQuery(): ResultSet = {
    checkOpen()
    resultSet = new FakeResultSet(this, getFakeResult(this.sql), resultSetType, resultSetConcurrency)
    resultSet
  }

  @throws(classOf[SQLException])
  override def getResultSet: ResultSet = {
    checkOpen()
    resultSet
  }

  @throws(classOf[SQLException])
  def executeUpdate(): Int = {
    checkOpen()
    1
  }

  @throws(classOf[SQLException])
  def setNull(parameterIndex: Int, sqlType: Int) {
    checkOpen()
  }

  @throws(classOf[SQLException])
  def setBoolean(parameterIndex: Int, x: Boolean) {
    checkOpen()
  }

  @throws(classOf[SQLException])
  def setByte(parameterIndex: Int, x: Byte) {
    checkOpen()
  }

  @throws(classOf[SQLException])
  def setShort(parameterIndex: Int, x: Short) {
    checkOpen()
  }

  @throws(classOf[SQLException])
  def setInt(parameterIndex: Int, x: Int) {
    checkOpen()
  }

  @throws(classOf[SQLException])
  def setLong(parameterIndex: Int, x: Long) {
    checkOpen()
  }

  @throws(classOf[SQLException])
  def setFloat(parameterIndex: Int, x: Float) {
    checkOpen()
  }

  @throws(classOf[SQLException])
  def setDouble(parameterIndex: Int, x: Double) {
    checkOpen()
  }

  @throws(classOf[SQLException])
  def setBigDecimal(parameterIndex: Int, x: java.math.BigDecimal) {
    checkOpen()
  }

  @throws(classOf[SQLException])
  def setString(parameterIndex: Int, x: String) {
    checkOpen()
  }

  @throws(classOf[SQLException])
  def setBytes(parameterIndex: Int, x: scala.Array[Byte]) {
    checkOpen()
  }

  @throws(classOf[SQLException])
  def setDate(parameterIndex: Int, x: java.sql.Date) {
    checkOpen()
  }

  @throws(classOf[SQLException])
  def setTime(parameterIndex: Int, x: java.sql.Time) {
    checkOpen()
  }

  @throws(classOf[SQLException])
  def setTimestamp(parameterIndex: Int, x: java.sql.Timestamp) {
    checkOpen()
  }

  @throws(classOf[SQLException])
  def setAsciiStream(parameterIndex: Int, x: java.io.InputStream,length: Int) {
    checkOpen()
  }

  /** @deprecated */
  @throws(classOf[SQLException])
  def setUnicodeStream(parameterIndex: Int, x: java.io.InputStream,length: Int) {
    checkOpen()
  }

  @throws(classOf[SQLException])
  def setBinaryStream(parameterIndex: Int, x: java.io.InputStream,length: Int) {
    checkOpen()
  }

  @throws(classOf[SQLException])
  def clearParameters() {
    checkOpen()
  }

  @throws(classOf[SQLException])
  def setObject(parameterIndex: Int, x: AnyRef, targetSqlType: Int, scale: Int) {
    checkOpen()
  }

  @throws(classOf[SQLException])
  def setObject(parameterIndex: Int, x: AnyRef, targetSqlType: Int) {
    checkOpen()
  }

  @throws(classOf[SQLException])
  def setObject(parameterIndex: Int, x: AnyRef) {
    checkOpen()
  }

  @throws(classOf[SQLException])
  def execute(): Boolean = {
    checkOpen()
    true
  }

  @throws(classOf[SQLException])
  def addBatch() {
    checkOpen()
  }

  @throws(classOf[SQLException])
  def setCharacterStream(parameterIndex: Int, reader: java.io.Reader, length: Int) {
    checkOpen()
  }

  @throws(classOf[SQLException])
  def setRef(parameterIndex: Int, x: Ref) {
    checkOpen()
  }

  @throws(classOf[SQLException])
  def setBlob(parameterIndex: Int, x: Blob) {
    checkOpen()
  }

  @throws(classOf[SQLException])
  def setClob(parameterIndex: Int, x: Clob) {
    checkOpen()
  }

  @throws(classOf[SQLException])
  def setArray(i: Int, x: Array) {
    checkOpen()
  }

  @throws(classOf[SQLException])
  def getMetaData: ResultSetMetaData = {
    checkOpen()
    null
  }

  @throws(classOf[SQLException])
  def setDate(parameterIndex: Int, x: java.sql.Date, cal: Calendar) {
    checkOpen()
  }

  @throws(classOf[SQLException])
  def setTime(parameterIndex: Int, x: java.sql.Time, cal: Calendar) {
    checkOpen()
  }

  @throws(classOf[SQLException])
  def setTimestamp(parameterIndex: Int, x: java.sql.Timestamp, cal: Calendar) {
    checkOpen()
  }

  @throws(classOf[SQLException])
  def setNull(paramIndex: Int, sqlType: Int, typeName: String) {
    checkOpen()
  }

  @throws(classOf[SQLException])
  override def getMoreResults(current: Int): Boolean = {
    throw new SQLException("Not implemented");
  }

  @throws(classOf[SQLException])
  override def getGeneratedKeys: ResultSet = {
    new FakeResultSet(this, null, resultSetType, resultSetConcurrency);
  }

  @throws(classOf[SQLException])
  override def executeUpdate(sql: String, autoGeneratedKeys: Int): Int = {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  override def executeUpdate(sql: String, columnIndexes: scala.Array[Int]): Int = {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  override def executeUpdate(sql: String, columnNames: scala.Array[String]): Int = {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  override def execute(sql: String, autoGeneratedKeys: Int): Boolean = {
    throw new SQLException("Not implemented");
  }

  @throws(classOf[SQLException])
  override def execute(sql: String, columnIndexes: scala.Array[Int]): Boolean = {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  override def execute(sql: String, columnNames: scala.Array[String]): Boolean = {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  override def getResultSetHoldability = {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def setURL(parameterIndex: Int, x: java.net.URL) {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def getParameterMetaData: java.sql.ParameterMetaData = {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def setRowId(parameterIndex: Int, value: RowId) {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def setNString(parameterIndex: Int, value: String) {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def setNCharacterStream(parameterIndex: Int, value: Reader, length: Long) {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def setNClob(parameterIndex: Int, value: NClob) {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def setNClob(parameterIndex: Int, value: Reader, length: Long) {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def setClob(parameterIndex: Int, value: Reader, length: Long) {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def setBlob(parameterIndex: Int, inputStream: InputStream, length: Long) {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def setBlob(parameterIndex: Int, inputStream: InputStream) {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def setSQLXML(parameterIndex: Int, value: SQLXML) {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def setAsciiStream(parameterIndex: Int, inputStream: InputStream, length: Long) {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def setAsciiStream(parameterIndex: Int, inputStream: InputStream) {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def setBinaryStream(parameterIndex: Int, inputStream: InputStream, length: Long) {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def setBinaryStream(parameterIndex: Int, inputStream: InputStream) {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def setCharacterStream(parameterIndex: Int, reader: Reader, length: Long) {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def setCharacterStream(parameterIndex: Int, reader: Reader) {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def setNCharacterStream(parameterIndex: Int, reader: Reader) {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def setClob(parameterIndex: Int, reader: Reader) {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def setNClob(parameterIndex: Int, reader: Reader) {
    throw new SQLException("Not implemented")
  }

  private[this] def getFakeResult(query: String): scala.Array[scala.Array[Any]] = {
    FakeContext.getQueryResult(this.conn.asInstanceOf[FakeConnection].host, query)
  }
}