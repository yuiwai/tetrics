package com.yuiwai.tetrics.pwa.global

import com.yuiwai.tetrics.pwa.game.GameScene.{BackToTop, GameCommand}
import com.yuiwai.tetrics.pwa.game.{HeaderView, TetricsView}
import com.yuiwai.tetrics.pwa.top.TopScene.TopCommand
import com.yuiwai.tetrics.pwa.top.{TopView, TopViewModel}
import com.yuiwai.tetrics.ui.GameViewModel
import com.yuiwai.yachiyo.ui
import com.yuiwai.yachiyo.ui.ViewModel
import japgolly.scalajs.react.extra.{Broadcaster, Listenable, OnUnmount}
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{BackendScope, ScalaComponent}

object Layout {
  final case class Props(broadcaster: Broadcaster[(ViewModel, Option[ui.Scene#Command => Unit])])
  final case class State(viewModel: ViewModel, commandHandler: ui.Scene#Command => Unit)
  case object EmptyViewModel extends ViewModel

  final class Backend(bs: BackendScope[Props, State]) extends OnUnmount {
    def render(p: Props, s: State): VdomElement = {
      s.viewModel match {
        case _: TopViewModel =>
          TopLayout.Component(TopLayout.Props(s.commandHandler.asInstanceOf[TopCommand => Unit]))
        case vm: GameViewModel =>
          GameLayout.Component(GameLayout.Props(vm, s.commandHandler.asInstanceOf[GameCommand => Unit]))
        case _ => <.div()
      }
    }
  }

  val Component = ScalaComponent
    .builder[Props]("Layout")
    .initialState(State(EmptyViewModel, _ => ()))
    .renderBackend[Backend]
    .configure(Listenable.listen(
      _.broadcaster,
      bs =>
        (x: (ViewModel, Option[ui.Scene#Command => Unit])) =>
          x._2.fold(bs.modState(_.copy(x._1)))(h => bs.modState(_.copy(x._1, h)))
    ))
    .build
}

object TopLayout {
  final case class Props(
    commandHandler: TopCommand => Unit
  )
  val Component = ScalaComponent
    .builder[Props]("TopLayout")
    .render_P { p => TopView.Component(TopView.Props(p.commandHandler)) }
    .build
}

object GameLayout {
  final case class Props(
    viewModel: GameViewModel,
    commandHandler: GameCommand => Unit
  )
  val Component = ScalaComponent
    .builder[Props]("GameLayout")
    .render_P { p =>
      <.div(
        ^.height := "100%",
        HeaderView.Props(() => p.commandHandler(BackToTop)).render,
        TetricsView.Props(p.viewModel).render
      )
    }
    .build
}
