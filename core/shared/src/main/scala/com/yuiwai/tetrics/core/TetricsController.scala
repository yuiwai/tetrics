package com.yuiwai.tetrics.core

@deprecated("use com.yuiwai.tetrics.app.Controller", "0.2.0")
trait TetricsController[E, C] {
  def input(event: E)(implicit ctx: C, setting: TetricsSetting): Unit
  protected def eventToAction(event: E): Option[TetricsAction]
}
