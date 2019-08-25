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
    }
    "tetrics" - {
      val tetrics = Tetrics()
      "put center" - {
        tetrics.putCenter(Block("1111", 2)).offset ==> Offset(4, 4)
        tetrics.putCenter(Block("1111", 4)).offset ==> Offset(3, 5)
        tetrics.putCenter(Block("100111", 3)).offset ==> Offset(4, 4)
      }
      "rotate" - {
        val o1 = Offset(4, 4)
        val o2 = o1 + Offset(Math.ceil((4 - 1) / 2.0).toInt, Math.ceil((1 - 4) / 2.0).toInt) // (6, 3)
        val o3 = o2 + Offset(Math.floor((1 - 4) / 2.0).toInt, Math.floor((4 - 1) / 2.0).toInt) // (4, 4)
        val o4 = o1 + Offset(Math.floor((4 - 1) / 2.0).toInt, Math.floor((1 - 4) / 2.0).toInt) // (5, 2)
        val o5 = o4 + Offset(Math.floor((1 - 4) / 2.0).toInt, Math.floor((4 - 1) / 2.0).toInt) // (3, 3)
        val o6 = o5 + Offset(Math.ceil((4 - 1) / 2.0).toInt, Math.ceil((1 - 4) / 2.0).toInt) // (5, 2)
        val t = tetrics.put(Block("1111", 4), o1)
        t.turnRight.offset ==> o2
        t.turnRight.turnRight.offset ==> o3
        t.turnRight.turnRight.turnRight.offset ==> o4
        t.turnLeft.offset ==> o4
        t.turnLeft.turnLeft.offset ==> o5
        t.turnLeft.turnLeft.turnLeft.offset ==> o6
      }
    }
    "tetrics ops with event" - {
      val tetrics = Tetrics()
      val ops = new TetricsOpsWithEvent {}
      "put" - {
        ops.act(tetrics, PutBlockAction(Block("1111", 2))) match {
          case (t, e) =>
            t.centralField.count ==> 4
            e ==> BlockPut(Block("1111", 2))
        }
      }
      "rotate" - {
        val a = tetrics.putCenter(Block("111010", 3))
        ops.act(a, TurnRightAction) match {
          case (t, e) =>
            t.centralField.count ==> 4
            e ==> BlockRotated(RotationRight)
        }
        ops.act(a, TurnLeftAction) match {
          case (t, e) =>
            t.centralField.count ==> 4
            e ==> BlockRotated(RotationLeft)
        }
        "no space to rotation" - {
          // TODO test
        }
      }
      "move" - {
        val a = tetrics.putCenter(Block("1111", 2))
        ops.act(a, MoveLeftAction) match {
          case (t, e) =>
            t.centralField.count ==> 4
            e ==> BlockMoved(MoveLeft)
        }
        "out of field" - {
          // TODO test
        }
      }
      "drop" - {
        val a = tetrics.putCenter(Block("1111", 1))
        ops.act(a, DropLeftAction) match {
          case (t, e) =>
            t.leftField.numRows ==> 1
            t.leftField.status ==> FieldStatusActive
            e ==> BlockDropped(FieldLeft)
        }
        "out of field" - {
          ops.act(ops.act(ops.act(a, DropBottomAction)._1, DropBottomAction)._1, DropBottomAction) match {
            case (t, e) =>
              t.bottomField.status ==> FieldStatusFrozen
              e ==> BlockDropped(FieldBottom)
          }
        }
      }
      "normalize" - {
        val a = tetrics.mapField(FieldBottom)(_.mapRow(0)(_ => Row("1111111111")))
        a.bottomField.numRows ==> 1
        ops.act(a, NormalizeBottomAction) match  {
          case (t, e) =>
            t.bottomField.numRows ==> 0
            e ==> FieldNormalized(FieldBottom, 1, 0)
        }
      }
    }
  }
}
