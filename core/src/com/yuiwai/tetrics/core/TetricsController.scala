package com.yuiwai.tetrics.core

trait TetricsController[E, C] {
  def input(event: E)(implicit ctx: C, setting: TetricsSetting): Unit
  protected def eventToAction(event: E): Option[TetricsAction]
}
sealed trait TetricsAction
sealed trait DropAction extends TetricsAction
case object MoveLeftAction extends TetricsAction
case object MoveRightAction extends TetricsAction
case object MoveUpAction extends TetricsAction
case object MoveDownAction extends TetricsAction
case object DropLeftAction extends TetricsAction with DropAction
case object DropRightAction extends TetricsAction with DropAction
case object DropTopAction extends TetricsAction with DropAction
case object DropBottomAction extends TetricsAction with DropAction
case object TurnLeftAction extends TetricsAction
case object TurnRightAction extends TetricsAction
