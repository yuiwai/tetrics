package com.yuiwai.tetrics.core

trait TetricsController[E, C] {
  def input(event: E)(implicit ctx: C, setting: TetricsSetting): Unit
  protected def eventToAction(event: E): Option[TetricsAction]
}
sealed trait TetricsAction
sealed trait MoveAction extends TetricsAction
sealed trait DropAction extends TetricsAction {
  val fieldType: FieldType
}
sealed trait RotateAction extends TetricsAction
case object MoveLeftAction extends TetricsAction with MoveAction
case object MoveRightAction extends TetricsAction with MoveAction
case object MoveUpAction extends TetricsAction with MoveAction
case object MoveDownAction extends TetricsAction with MoveAction
case object DropLeftAction extends TetricsAction with DropAction {
  override val fieldType: FieldType = FieldLeft
}
case object DropRightAction extends TetricsAction with DropAction {
  override val fieldType: FieldType = FieldRight
}
case object DropTopAction extends TetricsAction with DropAction {
  override val fieldType: FieldType = FieldTop
}
case object DropBottomAction extends TetricsAction with DropAction {
  override val fieldType: FieldType = FieldBottom
}
case object TurnLeftAction extends TetricsAction with RotateAction
case object TurnRightAction extends TetricsAction with RotateAction
case object NoAction extends TetricsAction