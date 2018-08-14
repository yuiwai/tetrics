package com.yuiwai.tetrics.core

trait TetricsController[E, C] {
  def input(event: E)(implicit ctx: C): Unit
  protected def eventToAction(event: E): Option[TetricsAction]
}
sealed trait TetricsAction
case object MoveLeftAction extends TetricsAction
case object MoveRightAction extends TetricsAction
case object MoveUpAction extends TetricsAction
case object MoveDownAction extends TetricsAction
case object DropLeftAction extends TetricsAction
case object DropRightAction extends TetricsAction
case object DropTopAction extends TetricsAction
case object DropBottomAction extends TetricsAction
case object TurnLeftAction extends TetricsAction
case object TurnRightAction extends TetricsAction

