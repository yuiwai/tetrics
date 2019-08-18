package com.yuiwai.tetrics.pwa

import com.yuiwai.tetrics.core._
import com.yuiwai.tetrics.pwa.game.{GamePresenter, GameScene, GameView}
import com.yuiwai.tetrics.pwa.global.Layout
import com.yuiwai.tetrics.pwa.top.{TopPresenter, TopScene, TopView}
import com.yuiwai.yachiyo.ui
import com.yuiwai.yachiyo.ui.{Application, SceneSuite, ViewModel}
import com.yuiwai.yachiyo.zio.ApplicationHandler
import com.yuiwai.yachiyo.zio.ApplicationHandler.AppEnv
import japgolly.scalajs.react.extra.Broadcaster
import org.scalajs.dom
import zio.{App, ZIO}

object PWAApp extends Application with App {
  implicit val setting: TetricsSetting = DefaultSettings.setting.copy(
    fieldWidth = 5,
    fieldHeight = 5,
  )
  val TopSceneKey = 1
  val GameSceneKey = 2
  trait AppBroadcaster extends Broadcaster[(ViewModel, Option[ui.Scene#Command => Unit])] {
    def send(viewModel: ViewModel, commandHandler: ui.Scene#Command => Unit): Unit =
      broadcast(viewModel -> Some(commandHandler)).runNow()
    def send(viewModel: ViewModel): Unit = broadcast(viewModel -> None).runNow()
  }
  val broadcaster = new AppBroadcaster {}
  override def initialSceneSuiteKey: Int = TopSceneKey
  override def run(args: List[String]): ZIO[PWAApp.Environment, Nothing, Unit] = {
    val layout = Layout.Component
    layout(Layout.Props(broadcaster)).renderIntoDOM(dom.document.getElementById("stage"))

    AppEnv.init(this)
      .flatMap(ApplicationHandler.program.provide)
      .fold(_ => (), _ => ())
  }
  override val sceneSuiteMap: Map[Int, SceneSuite] = Map(
    TopSceneKey -> SceneSuite(
      () => TopScene,
      () => TopPresenter,
      () => new TopView(broadcaster)
    ),
    GameSceneKey -> SceneSuite(
      () => new GameScene,
      () => new GamePresenter,
      () => new GameView(broadcaster)
    )
  )
}

