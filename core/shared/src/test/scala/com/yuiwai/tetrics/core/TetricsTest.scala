package com.yuiwai.tetrics.core

import utest._

object TetricsTest extends TestSuite {
  val tests = Tests {
    "field" - {
      val field = Field(10)
      "offset" - {
        field.offset ==> None
        field.put(Block("1", 1), 0, 0).offset ==> Some(Offset.zero)
        field.put(Block("1", 1), 0, 1).offset ==> Some(Offset(0, 1))
        field.put(Block("1", 1), 2, 3).offset ==> Some(Offset(2, 3))
      }
      "region" - {
        val block = field.put(Block("11", 2), 1, 0).region(Offset.zero, 2, 2)
        block ==> Block("0100", 2)
      }
      "trim" - {
        field.trim ==> (Block.empty, Offset.zero)
        field.put(Block("11", 2), 2, 3).trim ==> (Block("11", 2), Offset(2, 3))
        field.put(Block("1101", 2), 8, 8).trim ==> (Block("1101", 2), Offset(8, 8))
      }
    }
  }
}
