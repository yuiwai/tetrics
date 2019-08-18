package com.yuiwai.tetrics.pwa.game

import com.yuiwai.tetrics.core._
import com.yuiwai.tetrics.pwa.game.GameScene.{GameCommand, TetricsActionCommand}
import com.yuiwai.tetrics.ui.GameViewModel
import japgolly.scalajs.react.extra.OnUnmount
import japgolly.scalajs.react.{BackendScope, CallbackTo, ScalaComponent}

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
  def genButton(label: String, left: Int, top: Int, action: TetricsAction)
    (implicit p: Props): VdomElement = {
    <.button(
      ^.position := "absolute",
      ^.width := "25px",
      ^.textAlign := "center",
      ^.left := s"${left}px",
      ^.top := s"${top}px",
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
        genButton("⬅️", 10, 60, MoveLeftAction),
        genButton("➡️", 70, 60, MoveRightAction),
        genButton("⬆️", 40, 30, MoveUpAction),
        genButton("⬇️", 40, 90, MoveDownAction),

        genButton("↩️", 170, 0, TurnRightAction),
        genButton("↪️", 210, 0, TurnLeftAction),

        genButton("⏪", 160, 70, DropAndNormalizeAction(DropLeftAction, NormalizeLeftAction)),
        genButton("⏩️", 220, 70, DropAndNormalizeAction(DropRightAction, NormalizeRightAction)),
        genButton("⏫️", 190, 40, DropAndNormalizeAction(DropTopAction, NormalizeTopAction)),
        genButton("⏬️", 190, 100, DropAndNormalizeAction(DropBottomAction, NormalizeBottomAction)),
      )
    }
    .build
}
