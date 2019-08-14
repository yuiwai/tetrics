package com.yuiwai.tetrics.ui

import com.yuiwai.tetrics.core.Tetrics
import com.yuiwai.yachiyo.ui._

trait TetricsApplication extends Application {
  val GameSceneKey = 1
  def gameView: View
  override def initialSceneSuiteKey: Int = GameSceneKey
  override val sceneSuiteMap: Map[Int, SceneSuite] = Map(
    GameSceneKey -> SceneSuite(
      () => GameScene,
      () => GamePresenter,
      () => gameView
    )
  )
}

object GameScene extends Scene {
  override type State = Tetrics
  override type Command = TetricsCommand
  override type Event = None.type

  final case class TetricsCommand()

  override def initialState(): Tetrics = Tetrics()
  override def execute(state: Tetrics, input: TetricsCommand): (Tetrics, None.type, SceneCallback) = ???
}

object GamePresenter extends Presenter {
  override type S = GameScene.type
  override type M = GameViewModel
  override def updated(state: Tetrics, prevModel: GamePresenter.Prev): GameViewModel = ???
}

final case class GameViewModel() extends ViewModel

trait GameView extends View {
  override type S = GameScene.type
  override type M = GameViewModel
}
