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
  def fieldStats(fieldType: FieldType): FieldStats =
    fieldType match {
      case FieldLeft => leftField
      case FieldRight => rightField
      case FieldTop => topField
      case FieldBottom => bottomField
    }
  def modFieldStats(fieldType: FieldType)(f: FieldStats => FieldStats): TetricsStats =
    fieldType match {
      case FieldLeft => copy(leftField = f(leftField))
      case FieldRight => copy(rightField = f(rightField))
      case FieldTop => copy(topField = f(topField))
      case FieldBottom => copy(bottomField = f(bottomField))
    }
  def allFields: Seq[FieldStats] = FieldTypes.allDroppable.map(fieldStats)
  def mapFields(f: FieldStats => FieldStats): TetricsStats =
    FieldTypes.allDroppable.foldLeft(this) { case (acc, fieldType) => acc.modFieldStats(fieldType)(f) }
  def leftDeleted: Int = leftField.deletedRows
  def rightDeleted: Int = rightField.deletedRows
  def topDeleted: Int = topField.deletedRows
  def bottomDeleted: Int = bottomField.deletedRows
  def totalDeleted: Int = leftDeleted + rightDeleted + topDeleted + bottomDeleted
  def apply(event: TetricsEvent): TetricsStats = event match {
    case CompositeEvent(head, tail) => apply(head).apply(tail)
    case BlockAdded(_) => copy(blockCount = blockCount + 1)
    case BlockRotated(_) => copy(rotationCount = rotationCount + 1)
    case BlockMoved(_) => copy(moveCount = moveCount + 1)
    case BlockDropped(fieldType: FieldType, _, _) => modFieldStats(fieldType)(_.apply(event))
    case FieldNormalized(fieldType: FieldType, _) => modFieldStats(fieldType)(_.apply(event))
    case _ => this
  }
}
case class FieldStats(numRows: Int = 0, droppedBlocks: Int = 0, deletedRows: Int = 0) {
  def apply(event: TetricsEvent): FieldStats = event match {
    case BlockDropped(_, newNumRows, _) => copy(newNumRows, droppedBlocks + 1)
    case FieldNormalized(_, newNumRows) => copy(newNumRows, deletedRows = deletedRows + numRows - newNumRows)
    case _ => this
  }
}
