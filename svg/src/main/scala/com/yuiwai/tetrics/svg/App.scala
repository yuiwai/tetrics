package com.yuiwai.tetrics.svg

import com.yuiwai.tetrics.app._
import com.yuiwai.tetrics.core._
import japgolly.scalajs.react.Callback
import japgolly.scalajs.react.extra.Broadcaster
import org.scalajs.dom
import org.scalajs.dom.ext.KeyCode
import org.scalajs.dom.raw.KeyboardEvent

object App {
  implicit val setting = DefaultSettings.setting.copy(
    fieldWidth = 5,
    fieldHeight = 5,
  )
  def main(args: Array[String]): Unit = init()
  def init(): Unit = {
    implicit val eventBus: EventBus = EventBus()
    val gameView = GameView.Component
    val controller = new GameController
    val presenter = new GamePresenter
    val game = new SingleGame(presenter)
    gameView(GameView.Props(presenter)).renderIntoDOM(dom.document.getElementById("stage"))
    dom.document.body.onkeydown = { e =>
      controller(game, e)
    }
    dom.window.setTimeout(() => game.start(), 1000)
  }
  implicit class FieldDataEx(fieldData: FieldData) {
    import Colors._
    def color(x: Int, y: Int): String = color(Pos(x, y))
    def color(pos: Pos): String = if (fieldData.filled(pos)) RED else BLACK
  }
}

final class SingleGame(presenter: Presenter[FieldData])(implicit val setting: TetricsSetting) extends Game {
  implicit val eventBus: EventBus = EventBus()
  private var tetrics = Tetrics(setting.fieldWidth, setting.fieldHeight)
  override def start(): Game = {
    tetrics = draw(tetrics, randPut(tetrics))
    this
  }
  override def act(action: TetricsAction): Game = {
    action match {
      case _: DropAndNormalizeAction =>
        tetrics = draw(tetrics, randPut(tetrics.act(action)))
      case _ =>
        tetrics = draw(tetrics, tetrics.act(action))
    }
    this
  }
  def draw(before: Tetrics, after: Tetrics): Tetrics = {
    presenter.draw(diff(before, after))
    after
  }
  def diff(before: Tetrics, after: Tetrics): Map[FieldType, FieldData] = {
    Seq(FieldLeft, FieldRight, FieldTop, FieldBottom, FieldCentral).collect {
      case fieldType if before.field(fieldType) != after.field(fieldType) =>
        fieldType -> FieldData.fromField(after.field(fieldType))
    }.toMap
  }
  def randPut(tetrics: Tetrics)(implicit setting: TetricsSetting): Tetrics = {
    import setting.blocks
    tetrics.putCenter(blocks((Math.random() * blocks.size).toInt))
  }
}
final class GameController extends Controller[KeyboardEvent, Unit] {
  override def inputToAction(input: KeyboardEvent): Option[TetricsAction] = input.keyCode match {
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
final class GamePresenter(implicit setting: TetricsSetting) extends Presenter[FieldData] with Broadcaster[ViewModel] {
  import setting.{fieldWidth, fieldHeight}
  private val tSize = 15
  private val padding = 10
  private val tUnit = tSize + 1
  private var viewModel = ViewModel.empty.copy(
    tileSize = tSize,
    fieldWidth = fieldWidth,
    fieldHeight = fieldHeight,
    leftFieldPos = Pos(0, tUnit * fieldHeight + padding),
    rightFieldPos = Pos((tUnit * fieldWidth + padding) * 2, tUnit * fieldHeight + padding),
    topFieldPos = Pos(tUnit * fieldWidth + padding, 0),
    bottomFieldPos = Pos(tUnit * fieldWidth + padding, (tUnit * fieldHeight + padding) * 2),
    centralFieldPos = Pos(tUnit * fieldWidth + padding, tUnit * fieldHeight + padding)
  )
  override def draw(modifiedFields: Map[FieldType, FieldData]): Unit = {
    viewModel = viewModel.applyDiff(modifiedFields)
    broadcast(viewModel).runNow()
  }
  override def broadcast(vm: ViewModel): Callback = super.broadcast(vm)
}
final case class ViewModel(
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
) {
  def applyDiff(modifiedFields: Map[FieldType, FieldData]): ViewModel = modifiedFields.foldLeft(this) {
    case (viewModel, (fieldType, fieldData)) => fieldType match {
      case FieldLeft => viewModel.copy(leftFieldData = fieldData.rotateRight(fieldHeight))
      case FieldRight => viewModel.copy(rightFieldData = fieldData.rotateLeft(fieldWidth))
      case FieldTop => viewModel.copy(topFieldData = fieldData.rotateTwice(fieldWidth, fieldHeight))
      case FieldBottom => viewModel.copy(bottomFieldData = fieldData)
      case FieldCentral => viewModel.copy(centralFieldData = fieldData)
    }
  }
}
object ViewModel {
  def empty = ViewModel(
    0, 0, 0,
    Pos.zero, FieldData.empty,
    Pos.zero, FieldData.empty,
    Pos.zero, FieldData.empty,
    Pos.zero, FieldData.empty,
    Pos.zero, FieldData.empty
  )
}
object Colors {
  val RED = "#ff0000"
  val BLACK = "#000000"
}
