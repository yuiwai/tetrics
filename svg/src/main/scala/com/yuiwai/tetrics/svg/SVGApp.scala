package com.yuiwai.tetrics.svg

import com.yuiwai.tetrics.core._
import com.yuiwai.tetrics.ui.GameScene.TetricsActionCommand
import com.yuiwai.tetrics.ui._
import com.yuiwai.yachiyo.zio.ApplicationHandler
import com.yuiwai.yachiyo.zio.ApplicationHandler.AppEnv
import japgolly.scalajs.react.extra.Broadcaster
import org.scalajs.dom
import org.scalajs.dom.ext.KeyCode
import org.scalajs.dom.raw.KeyboardEvent
import zio.{App, ZIO}

object SVGApp extends TetricsApplication with App {
  implicit val setting = DefaultSettings.setting.copy(
    fieldWidth = 5,
    fieldHeight = 5,
  )
  override def gameScene: GameScene = new GameScene
  override def gamePresenter: GamePresenter = new SVGGamePresenter
  override def gameView: GameView = SVGGameView
  override def run(args: List[String]): ZIO[SVGApp.Environment, Nothing, Unit] =
    AppEnv.init(this).flatMap(ApplicationHandler.program.provide).fold(_ => (), _ => ())
  implicit class FieldDataEx(fieldData: FieldData) {
    import Colors._
    def color(x: Int, y: Int): String = color(Pos(x, y))
    def color(pos: Pos): String = if (fieldData.filled(pos)) RED else BLACK
  }
}

class SVGGamePresenter(implicit setting: TetricsSetting)
  extends GamePresenter {
  import setting.{fieldHeight, fieldWidth}
  private val tSize = 15
  private val padding = 10
  private val tUnit = tSize + 1
  def initialViewModel = GameViewModel.empty.copy(
    tileSize = tSize,
    fieldWidth = fieldWidth,
    fieldHeight = fieldHeight,
    leftFieldPos = Pos(0, tUnit * fieldHeight + padding),
    rightFieldPos = Pos((tUnit * fieldWidth + padding) * 2, tUnit * fieldHeight + padding),
    topFieldPos = Pos(tUnit * fieldWidth + padding, 0),
    bottomFieldPos = Pos(tUnit * fieldWidth + padding, (tUnit * fieldHeight + padding) * 2),
    centralFieldPos = Pos(tUnit * fieldWidth + padding, tUnit * fieldHeight + padding)
  )
  override def usePrevModel: Boolean = true
  override def setup(initialState: (Tetrics, Tetrics)): GameViewModel = initialViewModel
  override def updated(state: (Tetrics, Tetrics), prevModel: Prev): GameViewModel =
    prevModel.getOrElse(initialViewModel).applyDiff(diff(state._2, state._1))
}

object SVGGameView extends GameView with Broadcaster[GameViewModel] {
  override type M = GameViewModel
  def stage = dom.document.getElementById("stage")
  override def setup(viewModel: GameViewModel, listener: SVGGameView.Listener): Unit = {
    val gameView = TetricsView.Component
    gameView(TetricsView.Props(this)).renderIntoDOM(stage)
    dom.document.body.onkeydown = { e =>
      inputToAction(e).foreach(a => listener(TetricsActionCommand(a)))
    }
  }
  override def update(viewModel: GameViewModel): Unit = broadcast(viewModel).runNow()
  override def cleanup(): Unit = {
    stage.innerHTML = ""
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
}
