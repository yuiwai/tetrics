package com.yuiwai.tetrics.app

import com.yuiwai.tetrics.core._

trait Game {
  implicit val setting: TetricsSetting
  def start(): Game
  def act(action: TetricsAction): Game
  def randPut(tetrics: Tetrics): Tetrics = {
    import setting.blocks
    tetrics.putCenter(blocks((Math.random() * blocks.size).toInt)).tetrics
  }
}
trait Controller[I, C] {
  def apply(game: Game, input: I): Unit = inputToAction(input).foreach(game.act)
  def inputToAction(input: I): Option[TetricsAction]
}
trait Presenter[T] {
  def draw(modifiedFields: Map[FieldType, T])
}

final case class Pos(x: Int, y: Int) {
  def +(that: Pos): Pos = Pos(x + that.x, y + that.y)
}
object Pos {
  def zero = Pos(0, 0)
}
final case class FieldData(filled: Set[Pos]) {
  def map(f: Pos => Pos): FieldData = FieldData(filled.map(f))
  def apply(x: Int, y: Int): Boolean = apply(Pos(x, y))
  def apply(pos: Pos): Boolean = filled(pos)
  def rotateRight(fieldHeight: Int): FieldData = map(pos => Pos(fieldHeight - pos.y - 1, pos.x))
  def rotateLeft(fieldWidth: Int): FieldData = map(pos => Pos(pos.y, fieldWidth - pos.x - 1))
  def rotateTwice(fieldWidth: Int, fieldHeight: Int): FieldData =
    map(pos => Pos(fieldWidth - pos.x - 1, fieldHeight - pos.y - 1))
}
object FieldData {
  def empty: FieldData = FieldData(Set.empty)
  def fromField(field: TetricsField): FieldData = FieldData {
    (for {
      y <- 0 until field.height
      x <- 0 until field.rows(y).width
      if (field.rows(y).cols & 1 << x) != 0
    } yield Pos(x, y)).toSet
  }
}
