package com.yuiwai.tetrics

import com.yuiwai.tetrics.core.{Block, Field, Surface}
import utest._

object TetricsTests extends TestSuite {
  val tests = Tests {
    "block" - {
      "surface" - {
        assert(Block("1111", 4).surface.value == List(0, 0, 0, 0))
        assert(Block("1111", 2).surface.value == List(0, 0))
        assert(Block("010111", 3).surface.value == List(0, 0, 0))
        assert(Block("111010", 3).surface.value == List(0, -1, 0))
        assert(Block("110011", 3).surface.value == List(0, -1, -1))
        assert(Block("110101", 2).surface.value == List(0, -2))
      }
    }
    "field" - {
      "surface" - {
        assert(Field(5).surface.value == List(0, 0, 0, 0, 0))
        assert(Field(5).drop(Block("010111", 3), 0).surface.value == List(1, 2, 1, 0, 0))
      }
    }
    "surface" - {
      "lift" - {
        assert(Surface(List(1, 2, 3)).lift(1).value == List(2, 3, 4))
        assert(Surface(List(1, 2, 3)).lift(-1).value == List(0, 1, 2))
        assert(Surface(List(1, 2, 3)).lift(-2).value == List(-1, 0, 1))
      }
      "liftTo" - {
        assert(Surface(List(1, 2, 3)).liftTo(3).value == List(3, 4, 5))
        assert(Surface(List(1, 2, 3)).liftTo(0).value == List(0, 1, 2))
        assert(Surface(List(1, 2, 3)).liftTo(-1).value == List(-1, 0, 1))
      }
      "normalize" - {
        assert(Surface(List(1, 2, 3)).normalize.value == List(0, 1, 2))
        assert(Surface(List(2, 2, 3)).normalize.value == List(0, 0, 1))
      }
      "fitting" - {
        assert(Surface(List(1, 2, 3)).fitting(Surface(List(1, 2, 3))) == 0)
        assert(Surface(List(1, 1, 3)).fitting(Surface(List(1, 2, 3))) == -1)
        assert(Surface(List(1, 1, 2)).fitting(Surface(List(0, 1, 2))) == -2)
        assert(Surface(List(1, 2, 1)).fitting(Surface(List(0, 0, 0))) == -2)
        assert(Surface(List(0, 0, 0)).fitting(Surface(List(0, -1, -1))) == -1)
      }
    }
  }
}
