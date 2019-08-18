package com.yuiwai.tetrics.pwa.game

import com.yuiwai.tetrics.core._
import com.yuiwai.tetrics.pwa.PWAApp
import com.yuiwai.tetrics.pwa.PWAApp.AppBroadcaster
import com.yuiwai.tetrics.pwa.game.GameScene.{BackToTop, TetricsActionCommand, GameCommand}
import com.yuiwai.tetrics.ui
import com.yuiwai.tetrics.ui.{GameViewModel, Pos}
import com.yuiwai.yachiyo.ui.{NextSceneCallback, NoCallback, Scene, View}
import japgolly.scalajs.react.extra.Broadcaster
import org.scalajs.dom
import org.scalajs.dom.ext.KeyCode
import org.scalajs.dom.raw.{Element, KeyboardEvent}

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
      case BackToTop => (state, None, NextSceneCallback(PWAApp.TopSceneKey))
    }
}

class GamePresenter(implicit setting: TetricsSetting)
  extends ui.GamePresenter {
  override type S = GameScene
  import setting.{fieldHeight, fieldWidth}
  private val tSize = 15
  private val padding = 10
  private val tUnit = tSize + 1
  def initialViewModel: GameViewModel = GameViewModel.empty.copy(
    tileSize = tSize,
    fieldWidth = fieldWidth,
    fieldHeight = fieldHeight,
    leftFieldPos = Pos(0, tUnit * fieldHeight + padding),
    rightFieldPos = Pos((tUnit * fieldWidth + padding) * 2, tUnit * fieldHeight + padding),
    topFieldPos = Pos(tUnit * fieldWidth + padding, 0),
    bottomFieldPos = Pos(tUnit * fieldWidth + padding, (tUnit * fieldHeight + padding) * 2),
    centralFieldPos = Pos(tUnit * fieldWidth + padding, tUnit * fieldHeight + padding)
  )
  // override def usePrevModel: Boolean = true
  override def setup(initialState: (Tetrics, Tetrics)): GameViewModel = initialViewModel
  override def updated(state: (Tetrics, Tetrics), prevModel: Prev): GameViewModel =
    prevModel.getOrElse(initialViewModel).applyDiff(diff(state._2, state._1))
}

class GameView(broadcaster: AppBroadcaster) extends View {
  override type S = GameScene
  override type M = GameViewModel
  def stage: Element = dom.document.getElementById("stage")
  override def setup(viewModel: GameViewModel, listener: Listener): Unit = {
    broadcaster.send(viewModel, listener.asInstanceOf[Scene#Command => Unit])
    dom.document.body.onkeydown = { e =>
      inputToAction(e).foreach(a => listener(TetricsActionCommand(a)))
    }
  }
  override def update(viewModel: GameViewModel): Unit = broadcaster.send(viewModel)
  override def cleanup(): Unit = {
    dom.document.body.onkeydown = null
  }
  def inputToAction(input: KeyboardEvent): Option[TetricsAction] = input.keyCode match {
    case KeyCode.F => Some(TurnRightAction)
    case KeyCode.D => Some(TurnLeftAction)
    case KeyCode.Left | KeyCode.H =>
      if (input.shiftKey) Some(DropAndNormalizeAction(DropLeftAction, NormalizeLeftAction))
      else Some(MoveLeftAction)
    case KeyCode.Right | KeyCode.L =>
      if (input.shiftKey) Some(DropAndNormalizeAction(DropRightAction, NormalizeRightAction))
      else Some(MoveRightAction)
    case KeyCode.Up | KeyCode.K =>
      if (input.shiftKey) Some(DropAndNormalizeAction(DropTopAction, NormalizeTopAction))
      else Some(MoveUpAction)
    case KeyCode.Down | KeyCode.J =>
      if (input.shiftKey) Some(DropAndNormalizeAction(DropBottomAction, NormalizeBottomAction))
      else Some(MoveDownAction)
    case _ => None
  }
}
object Colors {
  val RED = "#ff0000"
  val BLACK = "#000000"
  implicit class FieldDataEx(fieldData: ui.FieldData) {
    def color(x: Int, y: Int): String = color(ui.Pos(x, y))
    def color(pos: ui.Pos): String = if (fieldData.filled(pos)) RED else BLACK
  }
}
