package com.yuiwai.tetrics.core

import utest._

object TetricsTest extends TestSuite {
  val tests = Tests {
    "block" - {
      "rotate" - {
        Block("1111", 4).turnLeft ==> Block("1111", 1)
        Block("1111", 4).turnRight ==> Block("1111", 1)
        Block("1111", 4).turnRight.turnLeft ==> Block("1111", 4)
        Block("1111", 4).turnRight.turnRight ==> Block("1111", 4)
      }
    }
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
        field.put(Block("0111", 2), 8, 8).trim ==> (Block("0111", 2), Offset(8, 8))
        field.put(Block("1110", 2), 8, 8).trim ==> (Block("1110", 2), Offset(8, 8))
      }
      "hitTest" - {
        field.hitTest(0, 0) ==> false
        field.put(Block("11", 2), 0, 0).hitTest(0, 0) ==> true
        field.put(Block("11", 2), 0, 0).hitTest(1, 0) ==> true
        field.put(Block("11", 2), 0, 0).hitTest(2, 0) ==> false
        field.put(Block("11", 2), 0, 0).hitTest(1, 1) ==> false
      }
      "turn" - {
        "with status" - {
          field.freeze.turnRight.status ==> FieldStatusFrozen
          field.freeze.turnLeft.status ==> FieldStatusFrozen
        }
      }
    }
    "tetrics" - {
      val tetrics = Tetrics()
      "put center" - {
        tetrics.putCenter(Block("1111", 2)).tetrics.offset ==> Offset(4, 4)
        tetrics.putCenter(Block("1111", 4)).tetrics.offset ==> Offset(3, 5)
        tetrics.putCenter(Block("100111", 3)).tetrics.offset ==> Offset(4, 4)
      }
      "rotate" - {
        val o1 = Offset(4, 4)
        val o2 = o1 + Offset(Math.ceil((4 - 1) / 2.0).toInt, Math.ceil((1 - 4) / 2.0).toInt) // (6, 3)
        val o3 = o2 + Offset(Math.floor((1 - 4) / 2.0).toInt, Math.floor((4 - 1) / 2.0).toInt) // (4, 4)
        val o4 = o1 + Offset(Math.floor((4 - 1) / 2.0).toInt, Math.floor((1 - 4) / 2.0).toInt) // (5, 2)
        val o5 = o4 + Offset(Math.floor((1 - 4) / 2.0).toInt, Math.floor((4 - 1) / 2.0).toInt) // (3, 3)
        val o6 = o5 + Offset(Math.ceil((4 - 1) / 2.0).toInt, Math.ceil((1 - 4) / 2.0).toInt) // (5, 2)
        val t = tetrics.put(Block("1111", 4), o1).tetrics
        t.turnRight.tetrics.offset ==> o2
        t.turnRight.tetrics.turnRight.tetrics.offset ==> o3
        t.turnRight.tetrics.turnRight.tetrics.turnRight.tetrics.offset ==> o4
        t.turnLeft.tetrics.offset ==> o4
        t.turnLeft.tetrics.turnLeft.tetrics.offset ==> o5
        t.turnLeft.tetrics.turnLeft.tetrics.turnLeft.tetrics.offset ==> o6
      }
    }
  }
}
