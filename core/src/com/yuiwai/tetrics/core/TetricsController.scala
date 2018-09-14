package com.yuiwai.tetrics.core

trait TetricsController[E, C] {
  def input(event: E)(implicit ctx: C, setting: TetricsSetting): Unit
  protected def eventToAction(event: E): Option[TetricsAction]
}
sealed trait TetricsAction
sealed trait MoveAction extends TetricsAction
sealed trait DropAction extends TetricsAction
sealed trait RotateAction extends TetricsAction
case object MoveLeftAction extends TetricsAction with MoveAction
case object MoveRightAction extends TetricsAction with MoveAction
case object MoveUpAction extends TetricsAction with MoveAction
case object MoveDownAction extends TetricsAction with MoveAction
case object DropLeftAction extends TetricsAction with DropAction
case object DropRightAction extends TetricsAction with DropAction
case object DropTopAction extends TetricsAction with DropAction
case object DropBottomAction extends TetricsAction with DropAction
case object TurnLeftAction extends TetricsAction with RotateAction
case object TurnRightAction extends TetricsAction with RotateAction
