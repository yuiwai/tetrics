package com.yuiwai.tetrics.pwa.top

import com.yuiwai.tetrics.pwa.PWAApp
import com.yuiwai.tetrics.pwa.PWAApp.AppBroadcaster
import com.yuiwai.tetrics.pwa.top.TopScene.{GoToGame, TopCommand}
import com.yuiwai.yachiyo.ui._
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{CallbackTo, ScalaComponent}

object TopScene extends Scene {
  override type State = None.type
  override type Command = TopCommand
  override type Event = None.type

  sealed trait TopCommand
  case object GoToGame extends TopCommand

  override def initialState(): None.type = None
  override def execute(state: None.type, input: TopCommand): Result = input match {
    case GoToGame => (None, None, NextSceneCallback(PWAApp.GameSceneKey))
    case _ => (None, None, NoCallback)
  }
}

object TopPresenter extends Presenter {
  override type S = TopScene.type
  override type M = TopViewModel
  override def updated(state: None.type, prevModel: TopPresenter.Prev): TopViewModel = TopViewModel()
}

final case class TopViewModel() extends ViewModel

class TopView(broadcaster: AppBroadcaster) extends View {
  override type S = TopScene.type
  override type M = TopViewModel
  override def setup(viewModel: TopViewModel, listener: Listener): Unit = {
    broadcaster.send(viewModel, listener.asInstanceOf[Scene#Command => Unit])
  }
  override def update(viewModel: TopViewModel): Unit = {}
  override def cleanup(): Unit = {}
}
object TopView {
  final case class Props(commandHandler: TopCommand => Unit)
  val Component = ScalaComponent
    .builder[Props]("TopView")
    .render_P { p =>
      <.div(
        <.button(
          ^.onClick --> CallbackTo(p.commandHandler(GoToGame)),
          "Start Game"
        )
      )
    }
    .build
}
