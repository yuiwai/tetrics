package com.yuiwai.tetrics.app

import com.yuiwai.tetrics.core.{FieldType, Tetrics, TetricsAction, TetricsSetting}

trait Game {
  implicit val setting: TetricsSetting
  def start(): Game
  def act(action: TetricsAction): Game
  def randPut(tetrics: Tetrics): Tetrics = {
    import setting.blocks
    tetrics.putCenter(blocks((Math.random() * blocks.size).toInt))
  }
}
trait Controller[I, C] {
  def apply(game: Game, input: I): Unit = inputToAction(input).foreach(game.act)
  def inputToAction(input: I): Option[TetricsAction]
}
trait Presenter[T] {
  def draw(modifiedFields: Map[FieldType, T])
}
