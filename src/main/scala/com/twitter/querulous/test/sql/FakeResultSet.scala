package com.twitter.querulous.test.sql

import java.sql._
import java.util.Calendar
import java.io.{InputStream, Reader}

class FakeResultSet(
  private[this] val stmt: Statement,
  private[this] val data: scala.Array[scala.Array[Any]],
  private[this] val resultSetType: Int,
  private[this] val resultSetConcurrency: Int
  ) extends ResultSet {

  private[this] var currentRow: Int = -1
  private[this] var open: Boolean = true

  def this(stmt: Statement) = {
    this(stmt, null, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)

  }

  def this(stmt: Statement, data: scala.Array[scala.Array[Any]]) = {
    this(stmt, data, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)
  }

  @throws(classOf[SQLException])
  def next(): Boolean = {
    checkOpen()
    if (this.data != null) {
      currentRow = currentRow + 1;
      currentRow < this.data.length;
    } else {
      false
    }
  }

  @throws(classOf[SQLException])
  def close() {
    open = false
  }

  @throws(classOf[SQLException])
  def wasNull(): Boolean = {
    checkOpen()
    false;
  }

  @throws(classOf[SQLException])
  def getString(columnIndex: Int): String = {
    checkOpen()
    data(currentRow)(columnIndex - 1).asInstanceOf[String]
  }

  @throws(classOf[SQLException])
  def getBoolean(columnIndex: Int): Boolean = {
    checkOpen()
    data(currentRow)(columnIndex - 1).asInstanceOf[Boolean]
  }

  @throws(classOf[SQLException])
  def getByte(columnIndex: Int): Byte = {
    checkOpen()
    data(currentRow)(columnIndex - 1).asInstanceOf[Byte]
  }

  @throws(classOf[SQLException])
  def getShort(columnIndex: Int): Short = {
    checkOpen()
    data(currentRow)(columnIndex - 1).asInstanceOf[Short]
  }

  @throws(classOf[SQLException])
  def getInt(columnIndex: Int): Int = {
    checkOpen()
    data(currentRow)(columnIndex - 1).asInstanceOf[Int]
  }

  @throws(classOf[SQLException])
  def getLong(columnIndex: Int): Long = {
    checkOpen()
    data(currentRow)(columnIndex - 1).asInstanceOf[Long]
  }

  @throws(classOf[SQLException])
  def getFloat(columnIndex: Int): Float = {
    checkOpen()
    data(currentRow)(columnIndex - 1).asInstanceOf[Float]
  }

  @throws(classOf[SQLException])
  def getDouble(columnIndex: Int): Double = {
    checkOpen()
    data(currentRow)(columnIndex - 1).asInstanceOf[Double]
  }

  /** @deprecated */
  @throws(classOf[SQLException])
  def getBigDecimal(columnIndex: Int): java.math.BigDecimal = {
    checkOpen()
    data(currentRow)(columnIndex - 1).asInstanceOf[java.math.BigDecimal]
  }

  @throws(classOf[SQLException])
  def getBytes(columnIndex: Int): scala.Array[Byte] = {
    checkOpen()
    data(currentRow)(columnIndex - 1).asInstanceOf[scala.Array[Byte]]
  }

  @throws(classOf[SQLException])
  def getDate(columnIndex: Int): java.sql.Date = {
    checkOpen()
    data(currentRow)(columnIndex - 1).asInstanceOf[java.sql.Date]
  }

  @throws(classOf[SQLException])
  def getTime(columnIndex: Int): java.sql.Time = {
    checkOpen()
    data(currentRow)(columnIndex - 1).asInstanceOf[java.sql.Time]
  }

  @throws(classOf[SQLException])
  def getTimestamp(columnIndex: Int): java.sql.Timestamp = {
    checkOpen()
    data(currentRow)(columnIndex - 1).asInstanceOf[java.sql.Timestamp]
  }

  @throws(classOf[SQLException])
  def getAsciiStream(columnIndex: Int): java.io.InputStream = {
    checkOpen()
    throw new SQLException("Not implemented")
  }

  /** @deprecated */
  @throws(classOf[SQLException])
  def getUnicodeStream(columnIndex: Int): java.io.InputStream = {
    checkOpen()
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def getBinaryStream(columnIndex: Int): java.io.InputStream = {
    checkOpen()
    throw new SQLException("Not implemented")
  }

  // TODO: need configuration to map column name to index
  @throws(classOf[SQLException])
  def getString(columnName: String): String = {
    checkOpen()
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def getBoolean(columnName: String): Boolean = {
    checkOpen()
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def getByte(columnName: String): Byte = {
    checkOpen()
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def getShort(columnName: String): Short = {
    checkOpen()
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def getInt(columnName: String): Int = {
    checkOpen()
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def getLong(columnName: String): Long = {
    checkOpen()
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def getFloat(columnName: String): Float = {
    checkOpen()
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def getDouble(columnName: String): Double = {
    checkOpen()
    throw new SQLException("Not implemented")
  }

  /** @deprecated */
  @throws(classOf[SQLException])
  def getBigDecimal(columnName: String, scale: Int): java.math.BigDecimal = {
    checkOpen()
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def getBytes(columnName: String): scala.Array[Byte] = {
    checkOpen()
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def getDate(columnName: String): java.sql.Date = {
    checkOpen()
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def getTime(columnName: String): java.sql.Time = {
    checkOpen()
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def getTimestamp(columnName: String): java.sql.Timestamp = {
    checkOpen()
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def getAsciiStream(columnName: String): java.io.InputStream = {
    checkOpen()
    throw new SQLException("Not implemented")
  }

  /** @deprecated */
  @throws(classOf[SQLException])
  def getUnicodeStream(columnName: String): java.io.InputStream = {
    checkOpen()
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def getBinaryStream(columnName: String): java.io.InputStream = {
    checkOpen()
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def getWarnings(): SQLWarning = {
    checkOpen()
    null
  }

  @throws(classOf[SQLException])
  def clearWarnings() {
    checkOpen()
  }

  @throws(classOf[SQLException])
  def getCursorName(): String = {
    checkOpen()
    ""
  }

  @throws(classOf[SQLException])
  def getMetaData(): ResultSetMetaData =  {
    checkOpen()
    null
  }

  @throws(classOf[SQLException])
  def getObject(columnIndex: Int): java.lang.Object = {
    checkOpen()
    data(currentRow)(columnIndex - 1) match {
      case c: AnyRef => c
      case _ => null
    }
  }

  @throws(classOf[SQLException])
  def getCharacterStream(columnIndex: Int): java.io.Reader = {
    checkOpen()
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def getBigDecimal(columnIndex: Int, scale: Int): java.math.BigDecimal = {
    checkOpen()
    throw new SQLException("Not implemented")
  }

  // TODO: this need to have a concrete implementation for further behavior simulation
  @throws(classOf[SQLException])
  def findColumn(columnName: String): Int ={
    checkOpen()
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def getObject(columnName: String): java.lang.Object = {
    checkOpen()
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def getCharacterStream(columnName: String): java.io.Reader = {
    checkOpen()
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def getBigDecimal(columnName: String): java.math.BigDecimal = {
    checkOpen()
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def isBeforeFirst(): Boolean = {
    checkOpen()
    currentRow == -1
  }

  @throws(classOf[SQLException])
  def isAfterLast(): Boolean = {
    checkOpen()
    if(data == null) {
      false
    } else {
      currentRow > data.length
    }
  }

  @throws(classOf[SQLException])
  def isFirst(): Boolean = {
    checkOpen()
    currentRow == 0
  }

  @throws(classOf[SQLException])
  def isLast(): Boolean = {
    checkOpen()
    if (data == null) {
      false
    } else {
      currentRow == data.length - 1
    }
  }

  @throws(classOf[SQLException])
  def beforeFirst() {
    checkOpen()
    currentRow = -1
  }

  @throws(classOf[SQLException])
  def afterLast() {
    checkOpen()
    if (data != null) {
      currentRow = data.length
    }
  }

  @throws(classOf[SQLException])
  def first(): Boolean = {
    checkOpen()
    if (data == null || data.length == 0) {
      false
    } else {
      currentRow = 0
      true
    }
  }

  @throws(classOf[SQLException])
  def last(): Boolean = {
    checkOpen()
    if (data == null || data.length == 0) {
      false
    } else {
      currentRow = data.length - 1
      true
    }
  }

  @throws(classOf[SQLException])
  def getRow: Int = {
      checkOpen()
      currentRow + 1
  }

  @throws(classOf[SQLException])
  def absolute(row: Int): Boolean = {
    checkOpen()
    false
  }

  @throws(classOf[SQLException])
  def relative(rows: Int): Boolean = {
    checkOpen()
    false
  }

  @throws(classOf[SQLException])
  def previous(): Boolean = {
    checkOpen()
    false
  }

  @throws(classOf[SQLException])
  def setFetchDirection(direction: Int) = {
    checkOpen()
  }

  @throws(classOf[SQLException])
  def getFetchDirection: Int = {
    checkOpen()
    ResultSet.FETCH_FORWARD
  }

  @throws(classOf[SQLException])
  def setFetchSize(rows: Int) {
    checkOpen()
  }

  @throws(classOf[SQLException])
  def getFetchSize(): Int = {
    checkOpen()
    1
  }

  @throws(classOf[SQLException])
  def getType: Int = {
    this.resultSetType
  }

  @throws(classOf[SQLException])
  def getConcurrency: Int = {
    this.resultSetConcurrency
  }

  @throws(classOf[SQLException])
  def rowUpdated(): Boolean = {
    checkOpen()
    false
  }

  @throws(classOf[SQLException])
  def rowInserted(): Boolean = {
    checkOpen()
    false
  }

  @throws(classOf[SQLException])
  def rowDeleted(): Boolean = {
    checkOpen()
    false
  }

  @throws(classOf[SQLException])
  def updateNull(columnIndex: Int) {
    checkOpen()
  }

  @throws(classOf[SQLException])
  def updateBoolean(columnIndex: Int, x: Boolean) {
    checkOpen()
  }

  @throws(classOf[SQLException])
  def updateByte(columnIndex: Int, x: Byte) {
    checkOpen()
  }

  @throws(classOf[SQLException])
  def updateShort(columnIndex: Int, x: Short) {
    checkOpen()
  }

  @throws(classOf[SQLException])
  def updateInt(columnIndex: Int, x: Int) {
    checkOpen()
  }

  @throws(classOf[SQLException])
  def updateLong(columnIndex: Int, x: Long) {
    checkOpen()
  }

  @throws(classOf[SQLException])
  def updateFloat(columnIndex: Int, x: Float) {
    checkOpen()
  }

  @throws(classOf[SQLException])
  def updateDouble(columnIndex: Int, x: Double) {
    checkOpen()
  }

  @throws(classOf[SQLException])
  def updateBigDecimal(columnIndex: Int, x: java.math.BigDecimal) {
    checkOpen()
  }

  @throws(classOf[SQLException])
  def updateString(columnIndex: Int, x: String) {
    checkOpen()
  }

  @throws(classOf[SQLException])
  def updateBytes(columnIndex: Int, x: scala.Array[Byte]) {
    checkOpen()
  }

  @throws(classOf[SQLException])
  def updateDate(columnIndex: Int, x: java.sql.Date) {
    checkOpen()
  }

  @throws(classOf[SQLException])
  def updateTime(columnIndex: Int, x: java.sql.Time) {
    checkOpen()
  }

  @throws(classOf[SQLException])
  def updateTimestamp(columnIndex: Int, x: java.sql.Timestamp) {
    checkOpen()
  }

  @throws(classOf[SQLException])
  def updateAsciiStream(columnIndex: Int, x: java.io.InputStream, length: Int) {
    checkOpen()
  }

  @throws(classOf[SQLException])
  def updateBinaryStream(columnIndex: Int, x: java.io.InputStream, length: Int) {
    checkOpen()
  }

  @throws(classOf[SQLException])
  def updateCharacterStream(columnIndex: Int, x: java.io.InputStream, length: Int) {
    checkOpen()
  }

  @throws(classOf[SQLException])
  def updateObject(columnIndex: Int, x: Any, length: Int) {
    checkOpen()
  }

  @throws(classOf[SQLException])
  def updateObject(columnIndex: Int, x: Any) {
    checkOpen()
  }

  @throws(classOf[SQLException])
  def updateNull(columnName: String) {
    checkOpen()
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def updateBoolean(columnName: String, x: Boolean) {
    checkOpen()
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def updateByte(columnName: String, x: Byte) {
    checkOpen()
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def updateShort(columnName: String, x: Short) {
    checkOpen()
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def updateInt(columnName: String, x: Int) {
    checkOpen()
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def updateLong(columnName: String, x: Long) {
    checkOpen()
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def updateFloat(columnName: String, x: Float) {
    checkOpen()
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def updateDouble(columnName: String, x: Double) {
    checkOpen()
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def updateBigDecimal(columnName: String, x: java.math.BigDecimal) {
    checkOpen()
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def updateString(columnName: String, x: String) {
    checkOpen()
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def updateBytes(columnName: String, x: scala.Array[Byte]) {
    checkOpen()
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def updateDate(columnName: String, x: java.sql.Date) {
    checkOpen()
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def updateTime(columnName: String, x: java.sql.Time) {
    checkOpen()
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def updateTimestamp(columnName: String, x: java.sql.Timestamp) {
    checkOpen()
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def updateAsciiStream(columnName: String, x: java.io.InputStream, length: Int) {
    checkOpen()
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def updateBinaryStream(columnName: String, x: java.io.InputStream, length: Int) {
    checkOpen()
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def updateCharacterStream(columnName: String, x: java.io.InputStream, length: Int) {
    checkOpen()
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def updateObject(columnName: String, x: Any, length: Int) {
    checkOpen()
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def updateObject(columnName: String, x: Any) {
    checkOpen()
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def insertRow() {
    checkOpen()
  }

  @throws(classOf[SQLException])
  def updateRow() {
    checkOpen()
  }

  @throws(classOf[SQLException])
  def deleteRow() {
    checkOpen()
  }

  @throws(classOf[SQLException])
  def refreshRow() {
    checkOpen()
  }

  @throws(classOf[SQLException])
  def cancelRowUpdates() {
    checkOpen()
  }

  @throws(classOf[SQLException])
  def moveToInsertRow() {
    checkOpen()
  }

  @throws(classOf[SQLException])
  def moveToCurrentRow() {
    checkOpen()
  }

  @throws(classOf[SQLException])
  def getStatement(): Statement = {
    checkOpen()
    stmt
  }

  @throws(classOf[SQLException])
  def getObject(i: Int, map: java.util.Map[String, Class[_]]): java.lang.Object = {
    checkOpen()
    null
  }

  @throws(classOf[SQLException])
  def getRef(i: Int): Ref = {
    checkOpen()
    null
  }

  @throws(classOf[SQLException])
  def getBlob(i: Int): Blob = {
    checkOpen()
    null
  }

  @throws(classOf[SQLException])
  def getClob(i: Int): Clob = {
    checkOpen()
    null
  }

  @throws(classOf[SQLException])
  def getArray(i: Int): Array = {
    checkOpen()
    null
  }

  @throws(classOf[SQLException])
  def getObject(colName: String, map: java.util.Map[String, Class[_]]): java.lang.Object = {
    checkOpen()
    throw new SQLException("Not Implemented")
  }

  @throws(classOf[SQLException])
  def getRef(colName: String): Ref = {
    checkOpen()
    throw new SQLException("Not Implemented")
  }

  @throws(classOf[SQLException])
  def getBlob(colName: String): Blob = {
    checkOpen()
    throw new SQLException("Not Implemented")
  }

  @throws(classOf[SQLException])
  def getClob(colName: String): Clob = {
    checkOpen()
    throw new SQLException("Not Implemented")
  }

  @throws(classOf[SQLException])
  def getArray(colName: String): Array = {
    checkOpen()
    throw new SQLException("Not Implemented")
  }

  @throws(classOf[SQLException])
  def getDate(columnIndex: Int, cal: Calendar): java.sql.Date = {
    checkOpen()
    null
  }

  @throws(classOf[SQLException])
  def getTime(columnIndex: Int, cal: Calendar): java.sql.Time = {
    checkOpen()
    null
  }

  @throws(classOf[SQLException])
  def getTimestamp(columnIndex: Int, cal: Calendar): java.sql.Timestamp = {
    checkOpen()
    null
  }

  @throws(classOf[SQLException])
  def getDate(columnName: String, cal: Calendar): java.sql.Date = {
    checkOpen()
    throw new SQLException("Not Implemented")
  }

  @throws(classOf[SQLException])
  def getTime(columnName: String, cal: Calendar): java.sql.Time = {
    checkOpen()
    throw new SQLException("Not Implemented")
  }

  @throws(classOf[SQLException])
  def getTimestamp(columnName: String, cal: Calendar): java.sql.Timestamp = {
    checkOpen()
    throw new SQLException("Not Implemented")
  }

  @throws(classOf[SQLException])
  def getURL(columnIndex: Int): java.net.URL = {
    checkOpen()
    null
  }

  @throws(classOf[SQLException])
  def getURL(columnName: String): java.net.URL = {
    checkOpen()
    throw new SQLException("Not Implemented")
  }

  @throws(classOf[SQLException])
  def updateRef(columnIndex: Int, x: java.sql.Ref) {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def updateBlob(columnIndex: Int, x: java.sql.Blob) {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def updateClob(columnIndex: Int, x: java.sql.Clob) {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def updateArray(columnIndex: Int, x: java.sql.Array) {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def updateRef(columnName: String, x: java.sql.Ref) {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def updateBlob(columnName: String, x: java.sql.Blob) {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def updateClob(columnName: String, x: java.sql.Clob) {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def updateArray(columnName: String, x: java.sql.Array) {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def isWrapperFor(iface: Class[_]): Boolean = {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def unwrap[T](iface: Class[T]): T = {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def getRowId(columnIndex: Int): RowId = {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def getRowId(columnName: String): RowId = {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def updateRowId(columnIndex: Int, value: RowId) {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def updateRowId(columnName: String, value: RowId) {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def getHoldability(): Int = {
    throw new SQLException("Not implemented")
  }

  @throws(classOf[SQLException])
  def isClosed(): Boolean = {
    return !open
  }

  @throws(classOf[SQLException])
  def updateNString(columnIndex: Int, value: String) {
    throw new SQLException("Not Implmented")
  }

  @throws(classOf[SQLException])
  def updateNString(columnName: String, value: String) {
    throw new SQLException("Not Implmented")
  }

  @throws(classOf[SQLException])
  def updateNClob(columnIndex: Int, value: NClob) {
    throw new SQLException("Not Implmented")
  }

  @throws(classOf[SQLException])
  def updateNClob(columnName: String, value: NClob) {
    throw new SQLException("Not Implmented")
  }

  @throws(classOf[SQLException])
  def getNClob(columnIndex: Int): NClob = {
    throw new SQLException("Not Implmented")
  }

  @throws(classOf[SQLException])
  def getNClob(columnName: String): NClob = {
    throw new SQLException("Not Implmented")
  }

  @throws(classOf[SQLException])
  def getSQLXML(columnIndex: Int): SQLXML = {
    throw new SQLException("Not Implmented")
  }

  @throws(classOf[SQLException])
  def getSQLXML(columnName: String): SQLXML = {
    throw new SQLException("Not Implmented")
  }

  @throws(classOf[SQLException])
  def updateSQLXML(columnIndex: Int, value: SQLXML) {
    throw new SQLException("Not Implmented")
  }

  @throws(classOf[SQLException])
  def updateSQLXML(columnName: String, value: SQLXML) {
    throw new SQLException("Not Implmented")
  }

  @throws(classOf[SQLException])
  def getNString(columnIndex: Int): String = {
    throw new SQLException("Not Implmented")
  }

  @throws(classOf[SQLException])
  def getNString(columnName: String): String = {
    throw new SQLException("Not Implmented")
  }

  @throws(classOf[SQLException])
  def getNCharacterStream(columnIndex: Int): Reader = {
    throw new SQLException("Not Implmented")
  }

  @throws(classOf[SQLException])
  def getNCharacterStream(columnName: String): Reader = {
    throw new SQLException("Not Implmented")
  }

  @throws(classOf[SQLException])
  def updateNCharacterStream(columnIndex: Int, reader: Reader, length: Long) {
    throw new SQLException("Not Implmented")
  }

  @throws(classOf[SQLException])
  def updateNCharacterStream(columnName: String, reader: Reader, length: Long) {
    throw new SQLException("Not Implmented")
  }

  @throws(classOf[SQLException])
  def updateAsciiStream(columnIndex: Int, inputStream: InputStream, length: Long) {
    throw new SQLException("Not Implmented")
  }

  @throws(classOf[SQLException])
  def updateBinaryStream(columnIndex: Int, inputStream: InputStream, length: Long) {
    throw new SQLException("Not Implmented")
  }

  @throws(classOf[SQLException])
  def updateCharacterStream(columnIndex: Int, reader: Reader, length: Int) {
    throw new SQLException("Not Implmented")
  }

  @throws(classOf[SQLException])
  def updateCharacterStream(columnIndex: Int, reader: Reader, length: Long) {
    throw new SQLException("Not Implmented")
  }

  @throws(classOf[SQLException])
  def updateAsciiStream(columnName: String, inputStream: InputStream, length: Long) {
    throw new SQLException("Not Implmented")
  }

  @throws(classOf[SQLException])
  def updateBinaryStream(columnName: String, inputStream: InputStream, length: Long) {
    throw new SQLException("Not Implmented")
  }

  @throws(classOf[SQLException])
  def updateCharacterStream(columnName: String, reader: Reader, length: Int) {
    throw new SQLException("Not Implmented")
  }

  @throws(classOf[SQLException])
  def updateCharacterStream(columnName: String, reader: Reader, length: Long) {
    throw new SQLException("Not Implmented")
  }

  @throws(classOf[SQLException])
  def updateBlob(columnIndex: Int, inputStream: InputStream, length: Long) {
    throw new SQLException("Not Implmented")
  }

  @throws(classOf[SQLException])
  def updateBlob(columnName: String, inputStream: InputStream, length: Long) {
    throw new SQLException("Not Implmented")
  }

  @throws(classOf[SQLException])
  def updateClob(columnIndex: Int, reader: Reader, length: Long) {
    throw new SQLException("Not Implmented")
  }

  @throws(classOf[SQLException])
  def updateClob(columnName: String, reader: Reader, length: Long) {
    throw new SQLException("Not Implmented")
  }

  @throws(classOf[SQLException])
  def updateNClob(columnIndex: Int, reader: Reader, length: Long) {
    throw new SQLException("Not Implmented")
  }

  @throws(classOf[SQLException])
  def updateNClob(columnName: String, reader: Reader, length: Long) {
    throw new SQLException("Not Implmented")
  }

  @throws(classOf[SQLException])
  def updateNCharacterStream(columnIndex: Int, reader: Reader) {
    throw new SQLException("Not Implmented")
  }

  @throws(classOf[SQLException])
  def updateNCharacterStream(columnName: String, reader: Reader) {
    throw new SQLException("Not Implmented")
  }

  @throws(classOf[SQLException])
  def updateAsciiStream(columnIndex: Int, inputStream: InputStream) {
    throw new SQLException("Not Implmented")
  }

  @throws(classOf[SQLException])
  def updateBinaryStream(columnIndex: Int, inputStream: InputStream) {
    throw new SQLException("Not Implmented")
  }

  @throws(classOf[SQLException])
  def updateCharacterStream(columnIndex: Int, reader: Reader) {
    throw new SQLException("Not Implmented")
  }

  @throws(classOf[SQLException])
  def updateAsciiStream(columnName: String, inputStream: InputStream) {
    throw new SQLException("Not Implmented")
  }

  @throws(classOf[SQLException])
  def updateBinaryStream(columnName: String, inputStream: InputStream) {
    throw new SQLException("Not Implmented")
  }

  @throws(classOf[SQLException])
  def updateCharacterStream(columnName: String, reader: Reader) {
    throw new SQLException("Not Implmented")
  }

  @throws(classOf[SQLException])
  def updateBlob(columnIndex: Int, inputStream: InputStream) {
    throw new SQLException("Not Implmented")
  }

  @throws(classOf[SQLException])
  def updateBlob(columnName: String, inputStream: InputStream) {
    throw new SQLException("Not Implmented")
  }

  @throws(classOf[SQLException])
  def updateClob(columnIndex: Int, reader: Reader) {
    throw new SQLException("Not Implmented")
  }

  @throws(classOf[SQLException])
  def updateClob(columnName: String, reader: Reader) {
    throw new SQLException("Not Implmented")
  }

  @throws(classOf[SQLException])
  def updateNClob(columnIndex: Int, reader: Reader) {
    throw new SQLException("Not Implmented")
  }

  @throws(classOf[SQLException])
  def updateNClob(columnName: String, reader: Reader) {
    throw new SQLException("Not Implmented")
  }

  @throws(classOf[SQLException])
  protected def checkOpen() {
    this.stmt.asInstanceOf[FakeStatement].checkOpen()

    if(!open) {
      throw new SQLException("ResultSet is closed.")
    }
  }
}