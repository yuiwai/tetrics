package com.yuiwai.tetrics.svg

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.svg_<^._

object FieldView {

  final case class Props(pos: Pos, cols: Int, rows: Int, size: Int, fieldData: FieldData) {
    @inline def render: VdomElement = Component(this)
  }

  final class Backend($: BackendScope[Props, Unit]) {
    def render(p: Props): VdomElement =
      <.g(
        (0 until p.rows).map { y =>
          <.g(
            (0 until p.cols).map { x =>
              <.rect(
                ^.fill := p.fieldData(x, y),
                ^.x := (p.size + 1) * x + p.pos.x,
                ^.y := (p.size + 1) * y + p.pos.y,
                ^.width := p.size,
                ^.height := p.size,
              )
            }.toTagMod
          )
        }.toTagMod
      )
  }

  val Component = ScalaComponent.builder[Props]("FieldView")
    .renderBackend[Backend]
    //.configure(Reusability.shouldComponentUpdate)
    .build
}