package com.yuiwai.tetrics.pwa.game

import com.yuiwai.tetrics.core._
import com.yuiwai.tetrics.pwa.game.GameScene.{GameCommand, TetricsActionCommand}
import com.yuiwai.tetrics.ui.GameViewModel
import japgolly.scalajs.react.extra.OnUnmount
import japgolly.scalajs.react.{BackendScope, Callback, CallbackTo, ScalaComponent}

object TetricsView {
  import japgolly.scalajs.react.vdom.svg_<^._
  final case class Props(viewModel: GameViewModel) {
    @inline def render: VdomElement = Component(this)
  }
  final case class State(viewModel: GameViewModel)
  object State {
    def init: State = State(GameViewModel.empty)
  }
  final class Backend(bs: BackendScope[Props, State]) extends OnUnmount {
    def render(p: Props, s: State): VdomElement = {
      import p.viewModel._
      <.svg(
        ^.width := 300,
        ^.height := 300,
        FieldView.Props(leftFieldPos, fieldWidth, fieldHeight, tileSize, leftFieldData).render,
        FieldView.Props(rightFieldPos, fieldWidth, fieldHeight, tileSize, rightFieldData).render,
        FieldView.Props(bottomFieldPos, fieldWidth, fieldHeight, tileSize, bottomFieldData).render,
        FieldView.Props(topFieldPos, fieldWidth, fieldHeight, tileSize, topFieldData).render,
        FieldView.Props(centralFieldPos, fieldWidth, fieldHeight, tileSize, centralFieldData).render,
      )
    }
  }

  val Component = ScalaComponent
    .builder[Props]("TetricsView")
    .initialState(State.init)
    .renderBackend[Backend]
    .build
}

object VController {
  import japgolly.scalajs.react.vdom.html_<^._
  final case class Props(commandHandler: GameCommand => Unit) {
    @inline def render: VdomElement = Component(this)
  }
  def genButton(label: String, left: Int, top: Int, action: TetricsAction, size: Int = 30, radius: Int = 0)
    (implicit p: Props): VdomElement = {
    <.button(
      ^.borderRadius := s"${radius}px",
      ^.position := "absolute",
      ^.width := s"${size}px",
      ^.height := s"${size}px",
      ^.textAlign := "center",
      ^.left := s"${left}px",
      ^.top := s"${top}px",
      ^.onSelect --> Callback(false),
      ^.onTouchStart --> CallbackTo(p.commandHandler(TetricsActionCommand(action))),
      label
    )
  }
  val Component = ScalaComponent
    .builder[Props]("VController")
    .render_P { implicit p =>
      <.div(
        ^.top := "10px",
        ^.position := "absolute",
        genButton("⬅️", 10, 70, MoveLeftAction),
        genButton("➡️", 90, 70, MoveRightAction),
        genButton("⬆️", 50, 30, MoveUpAction),
        genButton("⬇️", 50, 110, MoveDownAction),

        genButton("R", 250, -20, TurnRightAction, 40, 20),
        genButton("L", 10, -20, TurnLeftAction, 40, 20),

        genButton("⏪", 180, 70, DropAndNormalizeAction(DropLeftAction, NormalizeLeftAction)),
        genButton("⏩️", 260, 70, DropAndNormalizeAction(DropRightAction, NormalizeRightAction)),
        genButton("⏫️", 220, 30, DropAndNormalizeAction(DropTopAction, NormalizeTopAction)),
        genButton("⏬️", 220, 110, DropAndNormalizeAction(DropBottomAction, NormalizeBottomAction)),
      )
    }
    .build
}
