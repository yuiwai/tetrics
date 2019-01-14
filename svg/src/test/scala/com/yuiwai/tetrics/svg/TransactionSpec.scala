package com.yuiwai.tetrics.svg

import utest._

import scala.reflect.ClassTag

object TransactionSpec extends TestSuite with Transactional[Int] {

  val tests = Tests {
    "transactional" - {
      "committed" - {
        var counter = 0
        val transaction = {
          begin(Aggregation[Int, DummyEntity, Int](new DummyEntity, Seq(Publish(100, ClassTag(classOf[Int])))))
        }
        transaction.subscribe[Int](_ => counter = counter + 1)
        commit()
        counter ==> 1
      }
    }
  }
  class DummyEntity extends RootEntity[Int, Int] {
    override def applyCommand(command: Int): (DummyEntity.this.type, Int) = (this, command)
  }
}
