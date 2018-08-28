package com.yuiwai.tetrics.core

case class TetricsStats(
  blockCount: Int = 0,
  rotationCount: Int = 0,
  moveCount: Int = 0,
  leftField: FieldStats = FieldStats(),
  rightField: FieldStats = FieldStats(),
  topField: FieldStats = FieldStats(),
  bottomField: FieldStats = FieldStats()
) {
  def leftDeleted: Int = leftField.deletedRows
  def rightDeleted: Int = rightField.deletedRows
  def topDeleted: Int = topField.deletedRows
  def bottomDeleted: Int = bottomField.deletedRows
  def apply(event: TetricsEvent): TetricsStats = event match {
    case BlockAdded(_) =>
      copy(blockCount = blockCount + 1)
    case BlockRotated(_) =>
      copy(rotationCount = rotationCount + 1)
    case BlockMoved(_) =>
      copy(moveCount = moveCount + 1)
    case BlockDropped(fieldType: FieldType, _) =>
      fieldType match {
        case FieldLeft =>
          copy(leftField = leftField(event))
        case FieldRight =>
          copy(rightField = rightField(event))
        case FieldTop =>
          copy(topField = topField(event))
        case FieldBottom =>
          copy(bottomField = bottomField(event))
      }
    case FieldNormalized(fieldType: FieldType, _) =>
      fieldType match {
        case FieldLeft =>
          copy(leftField = leftField(event))
        case FieldRight =>
          copy(rightField = rightField(event))
        case FieldTop =>
          copy(topField = topField(event))
        case FieldBottom =>
          copy(bottomField = bottomField(event))
        case _ => this
      }
  }
}
case class FieldStats(numRows: Int = 0, droppedBlocks: Int = 0, deletedRows: Int = 0) {
  def apply(event: TetricsEvent): FieldStats = event match {
    case BlockDropped(_, newNumRows) => copy(newNumRows, droppedBlocks + 1)
    case FieldNormalized(_, newNumRows) => copy(newNumRows, deletedRows = deletedRows + numRows - newNumRows)
    case _ => this
  }
}
