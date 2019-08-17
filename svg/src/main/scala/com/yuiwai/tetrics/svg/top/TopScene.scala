package com.yuiwai.tetrics.svg.top

import com.yuiwai.yachiyo.ui.{Presenter, Scene, View}

import scala.None

object TopScene extends Scene {
  override type State = None.type
  override type Command = TopCommand
  override type Event = None.type

  sealed trait TopCommand

  override def initialState(): None.type = None
  override def execute(state: None.type, input: TopCommand): Result = ???
}

object TopPresenter extends Presenter {
  override type S = TopScene.type
  override type M = TopViewModel.type
  override def updated(state: None.type, prevModel: TopPresenter.Prev): TopViewModel.type = TopViewModel
}

object TopViewModel

object TopView extends View {
  override type S = TopScene.type
  override type M = TopViewModel.type
  override def setup(viewModel: TopViewModel.type , listener: TopView.Listener): Unit = ???
  override def update(viewModel: TopViewModel.type ): Unit = ???
  override def cleanup(): Unit = ???
}


