package com.yuiwai.tetrics.check

import com.yuiwai.tetrics.core._

object Main {
  def main(args: Array[String]): Unit = {
    assert(0 == Field(10).put(Block("010111", 3), 0, 8).slice(0, 3).spaces)
    assert(2 == Field(10).put(Block("111010", 3), 0, 8).slice(0, 3).spaces)
  }
  def spacing = {
    val oldTetrics = Tetrics()
    val newTetrics1 = oldTetrics.putCenter(Block("010111", 3)).dropBottom
    val newTetrics2 = oldTetrics.putCenter(Block("111010", 3)).dropBottom
    val newTetrics3 = oldTetrics.putCenter(Block("011101", 2)).dropBottom
    println(DefaultAutoPlayer().evalAction(oldTetrics, newTetrics1)(FieldBottom))
    println(DefaultAutoPlayer().evalAction(oldTetrics, newTetrics2)(FieldBottom))
    println(DefaultAutoPlayer().evalAction(oldTetrics, newTetrics3)(FieldBottom))
  }
}