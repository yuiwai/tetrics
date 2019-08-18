package com.yuiwai.tetrics.pwa.game

import com.yuiwai.tetrics.ui.GameViewModel
import japgolly.scalajs.react.extra.OnUnmount
import japgolly.scalajs.react.vdom.svg_<^._
import japgolly.scalajs.react.{BackendScope, ScalaComponent}

object TetricsView {
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
      val r = "↩"
      <.svg(
        ^.width := "100%",
        ^.height := "100%",
        FieldView.Props(leftFieldPos, fieldWidth, fieldHeight, tileSize, leftFieldData).render,
        FieldView.Props(rightFieldPos, fieldWidth, fieldHeight, tileSize, rightFieldData).render,
        FieldView.Props(bottomFieldPos, fieldWidth, fieldHeight, tileSize, bottomFieldData).render,
        FieldView.Props(topFieldPos, fieldWidth, fieldHeight, tileSize, topFieldData).render,
        FieldView.Props(centralFieldPos, fieldWidth, fieldHeight, tileSize, centralFieldData).render,
        <.text(^.fontSize := 20, ^.x := 10, ^.y := 300, ^.color := "black",  r)
      )
    }
  }

  val Component = ScalaComponent
    .builder[Props]("TetricsView")
    .initialState(State.init)
    .renderBackend[Backend]
    .build
}
