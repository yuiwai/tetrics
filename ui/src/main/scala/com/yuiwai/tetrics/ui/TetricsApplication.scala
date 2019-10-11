package com.yuiwai.tetrics.ui

import com.yuiwai.tetrics.core._
import com.yuiwai.yachiyo.ui._

import scala.util.Try

abstract class GameScene(implicit setting: TetricsSetting) extends Scene /* TODO settings should be from global state via initialState() */ {
  override type State = (Tetrics, Tetrics)

  override def initialState(): State = {
    val tetrics = Tetrics(setting.fieldWidth, setting.fieldHeight)
    randPut(tetrics) -> tetrics
  }
  def act(tetrics: Tetrics, action: TetricsAction): Tetrics = {
    action match {
      case _: DropAndNormalizeAction => randPut(tetrics.act(action))
      case _ => Try(tetrics.act(action)).fold(_ => tetrics, identity)
    }
  }
  def randPut(tetrics: Tetrics)(implicit setting: TetricsSetting): Tetrics = {
    import setting.blocks
    tetrics.putCenter(blocks((Math.random() * blocks.size).toInt))
  }
}

trait GamePresenter extends Presenter {
  override type M = GameViewModel
  def diff(before: Tetrics, after: Tetrics): Map[FieldType, FieldData] = {
    Seq(FieldLeft, FieldRight, FieldTop, FieldBottom, FieldCentral).collect {
      case fieldType if before.field(fieldType) != after.field(fieldType) =>
        fieldType -> FieldData.fromField(after.field(fieldType))
    }.toMap
  }
}

final case class GameViewModel(
  tileSize: Int,
  fieldWidth: Int,
  fieldHeight: Int,
  leftFieldPos: Pos,
  leftFieldData: FieldData,
  rightFieldPos: Pos,
  rightFieldData: FieldData,
  topFieldPos: Pos,
  topFieldData: FieldData,
  bottomFieldPos: Pos,
  bottomFieldData: FieldData,
  centralFieldPos: Pos,
  centralFieldData: FieldData,
) extends ViewModel {
  def applyDiff(modifiedFields: Map[FieldType, FieldData]): GameViewModel = modifiedFields.foldLeft(this) {
    case (viewModel, (fieldType, fieldData)) => fieldType match {
      case FieldLeft => viewModel.copy(leftFieldData = fieldData.rotateRight(fieldHeight))
      case FieldRight => viewModel.copy(rightFieldData = fieldData.rotateLeft(fieldWidth))
      case FieldTop => viewModel.copy(topFieldData = fieldData.rotateTwice(fieldWidth, fieldHeight))
      case FieldBottom => viewModel.copy(bottomFieldData = fieldData)
      case FieldCentral => viewModel.copy(centralFieldData = fieldData)
    }
  }
}
object GameViewModel {
  def empty = apply(
    0, 0, 0,
    Pos.zero, FieldData.empty,
    Pos.zero, FieldData.empty,
    Pos.zero, FieldData.empty,
    Pos.zero, FieldData.empty,
    Pos.zero, FieldData.empty
  )
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
