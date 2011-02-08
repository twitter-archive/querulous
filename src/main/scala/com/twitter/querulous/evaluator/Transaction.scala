package com.twitter.querulous.evaluator

import java.sql.{ResultSet, SQLException, SQLIntegrityConstraintViolationException, Connection}
import com.twitter.querulous.query.{QueryClass, QueryFactory, Query}

trait Transaction extends QueryEvaluator {
  def begin(): Unit

  def commit(): Unit

  def rollback(): Unit
}
