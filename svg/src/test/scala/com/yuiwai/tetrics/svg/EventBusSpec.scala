package com.yuiwai.tetrics.svg

import utest._

object EventBusSpec extends TestSuite with EventBus {
  val tests = Tests {
    "pub/sub" - {
      var counter = 0
      subscribe[Int](_ => counter += 1)
      publish(100)
      publish(200)
      counter ==> 2
    }
  }
}
