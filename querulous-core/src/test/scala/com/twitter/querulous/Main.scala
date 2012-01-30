package com.twitter.querulous

import collection.immutable.{StringOps, WrappedString}


object Main {

  def main(args: Array[String]) {
    val input = "*/ i am fucking with you"
    val replace = "*/"
    val replaceWith = "\\*\\/"
    println(input.replace(replace, replaceWith))
  }

}
