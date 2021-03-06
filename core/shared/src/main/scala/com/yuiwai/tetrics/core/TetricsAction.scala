package com.yuiwai.tetrics.core

sealed trait TetricsAction
sealed trait MoveAction extends TetricsAction
sealed trait DropAction extends TetricsAction {
  val fieldType: FieldType
  def withNormalize: DropAndNormalizeAction
}
sealed trait NormalizeAction extends TetricsAction {
  val fieldType: FieldType
}
sealed trait RotateAction extends TetricsAction
case object MoveLeftAction extends TetricsAction with MoveAction
case object MoveRightAction extends TetricsAction with MoveAction
case object MoveUpAction extends TetricsAction with MoveAction
case object MoveDownAction extends TetricsAction with MoveAction
case object DropLeftAction extends TetricsAction with DropAction {
  override val fieldType: FieldType = FieldLeft
  override def withNormalize: DropAndNormalizeAction = DropAndNormalizeAction(this, NormalizeLeftAction)
}
case object DropRightAction extends TetricsAction with DropAction {
  override val fieldType: FieldType = FieldRight
  override def withNormalize: DropAndNormalizeAction = DropAndNormalizeAction(this, NormalizeRightAction)
}
case object DropTopAction extends TetricsAction with DropAction {
  override val fieldType: FieldType = FieldTop
  override def withNormalize: DropAndNormalizeAction = DropAndNormalizeAction(this, NormalizeTopAction)
}
case object DropBottomAction extends TetricsAction with DropAction {
  override val fieldType: FieldType = FieldBottom
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
case object TurnLeftAction extends TetricsAction with RotateAction
case object TurnRightAction extends TetricsAction with RotateAction
final case class DropAndNormalizeAction(dropAction: DropAction, normalizeAction: NormalizeAction) extends TetricsAction {
  def fieldType: FieldType = dropAction.fieldType
}
case object NoAction extends TetricsAction