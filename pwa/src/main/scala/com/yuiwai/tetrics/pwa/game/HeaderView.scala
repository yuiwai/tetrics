package com.yuiwai.tetrics.pwa.game

import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{Callback, ScalaComponent}

object HeaderView {
  final case class Props(backToTopHandler: () => Unit) {
    @inline def render: VdomElement = Component(this)
  }

  val Component = ScalaComponent
    .builder[Props]("HeaderView")
    .render_P(p =>
      <.div(
        <.button(
          ^.onClick --> Callback {
            p.backToTopHandler()
          },
          "< Back"
        )
      )
    )
    .build
}
