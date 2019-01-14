package com.yuiwai.tetrics.svg

import utest._

object AggregationSpec extends TestSuite {
  val tests = Tests {
    "apply command" - {
      Aggregation[Int, Item, Int](new Item).apply(100).apply(200)
        .events.map(_.value) ==> Seq(100, 200)
    }
  }
  class Item extends RootEntity[Int, Int] {
    override def applyCommand(command: Int): (Item.this.type, Int) = (this, command)
  }
}
