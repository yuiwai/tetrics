package com.yuiwai.tetrics.svg

import japgolly.scalajs.react._
import japgolly.scalajs.react.extra._
import japgolly.scalajs.react.vdom.svg_<^._

object GameView {
  final case class Props(broadcaster: Broadcaster[ViewModel]) {
    @inline def render: VdomElement = Component(this)
  }
  final case class State(viewModel: ViewModel)
  object State {
    def init: State = State(ViewModel.empty)
  }
  final class Backend(bs: BackendScope[Props, State]) extends OnUnmount {
    def render(p: Props, s: State): VdomElement = {
      import s.viewModel._
      <.svg(
        ^.width := "100%",
        ^.height := "100%",
        FieldView.Props(leftFieldPos, fieldWidth, fieldHeight, tileSize, leftFieldData).render,
        FieldView.Props(centralFieldPos, fieldWidth, fieldHeight, tileSize, centralFieldData).render,
      )
    }
  }

  val Component = ScalaComponent
    .builder[Props]("MainView")
    .initialState(State.init)
    .renderBackend[Backend]
    .configure(Listenable.listen(_.broadcaster, bs => (vm: ViewModel) => bs.modState(_.copy(viewModel = vm))))
    .build
}
