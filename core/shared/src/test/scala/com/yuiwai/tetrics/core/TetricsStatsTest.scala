package com.yuiwai.tetrics.core

import utest._

object TetricsStatsTest extends TestSuite {
  val tests = Tests {
    "modFieldStats" - {
      val stats = TetricsStats()
        .modFieldStats(FieldLeft)(_.copy(droppedBlocks = 5))
      stats.fieldStats(FieldLeft).droppedBlocks ==> 5
      stats.fieldStats(FieldRight).droppedBlocks ==> 0
      stats.fieldStats(FieldTop).droppedBlocks ==> 0
      stats.fieldStats(FieldBottom).droppedBlocks ==> 0
    }
    "mapFields" - {
      val fields = TetricsStats()
        .mapFields(_.copy(numRows = 10))
        .allFields
      fields.size ==> 4
      fields.forall(_.numRows == 10) ==> true
    }
    "apply event" - {
      "dropped" - {
        val stats = TetricsStats()(BlockDropped(FieldRight, 0, Seq.empty))
        stats.fieldStats(FieldLeft).droppedBlocks ==> 0
        stats.fieldStats(FieldRight).droppedBlocks ==> 1
        stats.fieldStats(FieldTop).droppedBlocks ==> 0
        stats.fieldStats(FieldBottom).droppedBlocks ==> 0
      }
      "normalized" - {
        val stats = TetricsStats()
          .mapFields(_.copy(numRows = 2))(FieldNormalized(FieldBottom, 0))
        stats.fieldStats(FieldLeft).deletedRows ==> 0
        stats.fieldStats(FieldRight).deletedRows ==> 0
        stats.fieldStats(FieldTop).deletedRows ==> 0
        stats.fieldStats(FieldBottom).deletedRows ==> 2
      }
    }
  }
}
