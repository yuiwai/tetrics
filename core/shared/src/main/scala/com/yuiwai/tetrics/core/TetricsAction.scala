package com.yuiwai.tetrics.core

sealed trait TetricsAction
final case class PutBlockAction(block: Block) extends TetricsAction
sealed trait MoveAction extends TetricsAction {
  val moveType: MoveType
}
sealed trait DropAction extends TetricsAction {
  val fieldType: DroppableField
  def withNormalize: DropAndNormalizeAction
}
sealed trait NormalizeAction extends TetricsAction {
  val fieldType: FieldType
}
sealed trait RotateAction extends TetricsAction {
  val rotationType: RotationType
}
case object MoveLeftAction extends TetricsAction with MoveAction {
  override val moveType: MoveType = MoveLeft
}
case object MoveRightAction extends TetricsAction with MoveAction {
  override val moveType: MoveType = MoveRight
}
case object MoveUpAction extends TetricsAction with MoveAction {
  override val moveType: MoveType = MoveUp
}
case object MoveDownAction extends TetricsAction with MoveAction {
  override val moveType: MoveType = MoveDown
}
case object DropLeftAction extends TetricsAction with DropAction {
  override val fieldType: DroppableField = FieldLeft
  override def withNormalize: DropAndNormalizeAction = DropAndNormalizeAction(this, NormalizeLeftAction)
}
case object DropRightAction extends TetricsAction with DropAction {
  override val fieldType: DroppableField = FieldRight
  override def withNormalize: DropAndNormalizeAction = DropAndNormalizeAction(this, NormalizeRightAction)
}
case object DropTopAction extends TetricsAction with DropAction {
  override val fieldType: DroppableField = FieldTop
  override def withNormalize: DropAndNormalizeAction = DropAndNormalizeAction(this, NormalizeTopAction)
}
case object DropBottomAction extends TetricsAction with DropAction {
  override val fieldType: DroppableField = FieldBottom
  override def withNormalize: DropAndNormalizeAction = DropAndNormalizeAction(this, NormalizeBottomAction)
}
case object NormalizeLeftAction extends TetricsAction with NormalizeAction {
  override val fieldType: FieldType = FieldLeft
}
case object NormalizeRightAction extends TetricsAction with NormalizeAction {
  override val fieldType: FieldType = FieldRight
}
case object NormalizeTopAction extends TetricsAction with NormalizeAction {
  override val fieldType: FieldType = FieldTop
}
case object NormalizeBottomAction extends TetricsAction with NormalizeAction {
  override val fieldType: FieldType = FieldBottom
}
case object TurnLeftAction extends TetricsAction with RotateAction {
  override val rotationType: RotationType = RotationLeft
}
case object TurnRightAction extends TetricsAction with RotateAction {
  override val rotationType: RotationType = RotationRight
}
final case class DropAndNormalizeAction(dropAction: DropAction, normalizeAction: NormalizeAction) extends TetricsAction {
  def fieldType: FieldType = dropAction.fieldType
}
case object NoAction extends TetricsAction
