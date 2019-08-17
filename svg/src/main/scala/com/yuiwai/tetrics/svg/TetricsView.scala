package com.yuiwai.tetrics.svg

import com.yuiwai.tetrics.ui.GameViewModel
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra._
import japgolly.scalajs.react.vdom.svg_<^._

object TetricsView {
  final case class Props(broadcaster: Broadcaster[GameViewModel]) {
    @inline def render: VdomElement = Component(this)
  }
  final case class State(viewModel: GameViewModel)
  object State {
    def init: State = State(GameViewModel.empty)
  }
  final class Backend(bs: BackendScope[Props, State]) extends OnUnmount {
    def render(p: Props, s: State): VdomElement = {
      import s.viewModel._
      <.svg(
        ^.width := "100%",
        ^.height := "100%",
        FieldView.Props(leftFieldPos, fieldWidth, fieldHeight, tileSize, leftFieldData).render,
        FieldView.Props(rightFieldPos, fieldWidth, fieldHeight, tileSize, rightFieldData).render,
        FieldView.Props(bottomFieldPos, fieldWidth, fieldHeight, tileSize, bottomFieldData).render,
        FieldView.Props(topFieldPos, fieldWidth, fieldHeight, tileSize, topFieldData).render,
        FieldView.Props(centralFieldPos, fieldWidth, fieldHeight, tileSize, centralFieldData).render,
      )
    }
  }

  val Component = ScalaComponent
    .builder[Props]("MainView")
    .initialState(State.init)
    .renderBackend[Backend]
    .configure(Listenable.listen(_.broadcaster, bs => (vm: GameViewModel) => bs.modState(_.copy(viewModel = vm))))
    .build
}
