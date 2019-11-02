package com.yuiwai.tetrics.native

import com.yuiwai.tetrics.core._
import com.yuiwai.tetrics.native.GameScene.{GameCommand, TetricsActionCommand}
import com.yuiwai.tetrics.ui
import com.yuiwai.tetrics.ui.{FieldData, GameViewModel, Pos}
import com.yuiwai.yachiyo.ui.{NoCallback, View}

import scala.scalanative.native.CInt
import scala.scalanative.native._

object GameScene {
  sealed trait GameCommand
  final case class TetricsActionCommand(action: TetricsAction) extends GameCommand
  case object BackToTop extends GameCommand
}
class GameScene(implicit setting: TetricsSetting) extends ui.GameScene {
  override type Command = GameCommand
  override type Event = None.type
  override def execute(state: State, input: GameCommand): Result =
    input match {
      case TetricsActionCommand(action) => (act(state._1, action) -> state._2, None, NoCallback)
      // case BackToTop => (state, None, NextSceneCallback(PWAApp.TopSceneKey))
    }
}

class GamePresenter(implicit setting: TetricsSetting)
  extends ui.GamePresenter {
  override type S = GameScene
  import setting.{fieldHeight, fieldWidth}
  private val tSize = 1
  private val offset = 1
  private val padding = 1
  private val tUnit = tSize + 1
  def initialViewModel: GameViewModel = GameViewModel.empty.copy(
    tileSize = tSize,
    fieldWidth = fieldWidth,
    fieldHeight = fieldHeight,
    leftFieldPos = Pos(offset, offset + fieldHeight + padding),
    rightFieldPos = Pos(offset + (tUnit * fieldWidth + padding) * 2, offset + fieldHeight + padding),
    topFieldPos = Pos(offset + tUnit * fieldWidth + padding, offset),
    bottomFieldPos = Pos(offset + tUnit * fieldWidth + padding, offset + (fieldHeight + padding) * 2),
    centralFieldPos = Pos(offset + tUnit * fieldWidth + padding, offset + fieldHeight + padding)
  )
  // override def usePrevModel: Boolean = true
  override def setup(initialState: (Tetrics, Tetrics)): GameViewModel = initialViewModel
  override def updated(state: (Tetrics, Tetrics), prevModel: Prev): GameViewModel =
    prevModel.getOrElse(initialViewModel).applyDiff(diff(state._2, state._1))
}

class GameView extends View {
  override type S = GameScene
  override type M = GameViewModel
  override def setup(viewModel: GameViewModel, listener: Listener): Unit = {
    update(viewModel)
  }
  override def update(viewModel: GameViewModel): Unit = {
    import viewModel._
    drawField(fieldWidth, fieldHeight, centralFieldData, centralFieldPos.x, centralFieldPos.y)
    drawField(fieldWidth, fieldHeight, leftFieldData, leftFieldPos.x, leftFieldPos.y)
    drawField(fieldWidth, fieldHeight, rightFieldData, rightFieldPos.x, rightFieldPos.y)
    drawField(fieldWidth, fieldHeight, topFieldData, topFieldPos.x, topFieldPos.y)
    drawField(fieldWidth, fieldHeight, bottomFieldData, bottomFieldPos.x, bottomFieldPos.y)
  }
  override def cleanup(): Unit = {}
  import Ncurses._
  def drawField(fieldWidth: Int, fieldHeight: Int, fieldData: FieldData, offsetX: Int, offsetY: Int): Unit = {
    val pair1 = 1.toShort
    val pair2 = 2.toShort
    start_color()
    init_pair(pair1, Colors.COLOR_BLACK, Colors.COLOR_YELLOW)
    init_pair(pair2, Colors.COLOR_BLACK, Colors.COLOR_RED)
    (0 until fieldHeight).foreach { y =>
      (0 until fieldWidth) foreach { x =>
        if (fieldData(x, y)) {
          attron(COLOR_PAIR(pair2))
          mvprintw(y + offsetY, x * 2 + offsetX, c"  ")
          attroff(COLOR_PAIR(pair2))
        } else {
          attron(COLOR_PAIR(pair1))
          mvprintw(y + offsetY, x * 2 + offsetX, c"  ")
          attroff(COLOR_PAIR(pair1))
        }
      }
    }
  }
}
/*
object Colors {
  val RED = "#ff0000"
  val BLACK = "#000000"
  implicit class FieldDataEx(fieldData: ui.FieldData) {
    def color(x: Int, y: Int): String = color(ui.Pos(x, y))
    def color(pos: ui.Pos): String = if (fieldData.filled(pos)) RED else BLACK
  }
}
 */

object NativeEventHandler {
  def inputToAction(event: CInt): Option[TetricsAction] = {
    event match {
      // D
      case 100 => Some(TurnLeftAction)
      // F
      case 102 => Some(TurnRightAction)
      // H
      case 104 => Some(MoveLeftAction)
      case 72 => Some(DropAndNormalizeAction(DropLeftAction, NormalizeLeftAction))
      // J
      case 106 => Some(MoveDownAction)
      case 74 => Some(DropAndNormalizeAction(DropBottomAction, NormalizeBottomAction))
      // K
      case 107 => Some(MoveUpAction)
      case 75 => Some(DropAndNormalizeAction(DropTopAction, NormalizeTopAction))
      // L
      case 108 => Some(MoveRightAction)
      case 76 => Some(DropAndNormalizeAction(DropRightAction, NormalizeRightAction))
      // Other
      case _ => None
    }
  }
}
