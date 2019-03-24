package com.yuiwai.tetrics.svg

import utest._

object RepositorySpec extends TestSuite
  with Repository[Item] with IncrementalIdGenerator[Item] {
  val tests = Tests {
    "create" - {
      size ==> 0
      create(new Item).id ==> 1
      size ==> 1
    }
  }
}

class Item extends Entity
