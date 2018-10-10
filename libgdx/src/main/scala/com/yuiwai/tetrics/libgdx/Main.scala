package com.yuiwai.tetrics.libgdx

import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.{InputEvent, InputListener, Stage}
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.{ApplicationListener, Gdx}
import com.yuiwai.tetrics.core._

object Main extends Subscriber with DefaultSettings {
  def main(args: Array[String]): Unit = {
    // TODO 定数化
    new LwjglApplication(new AppListener, "tetrics", 520, 520)
  }
}

class AppListener extends ApplicationListener with DefaultSettings {
  implicit val eventBus = EventBus()
  implicit val ctx = new GdxContext {}
  // TODO 定数化
  lazy val stage: Stage = new Stage(new FitViewport(520, 520))
  private var game: TetricsGame[Char, GdxContext] = _
  override def create(): Unit = {
    Gdx.input.setInputProcessor(stage)
    game = new TenTen[Char, GdxContext] with GdxController with GdxView
    stage.addListener(new InputListener {
      override def keyTyped(event: InputEvent, character: Char): Boolean = {
        game.input(character)
        super.keyTyped(event, character)
      }
    })
    game.start()
  }
  override def resize(width: Int, height: Int): Unit = {
    stage.getViewport.update(width, height)
  }
  override def render(): Unit = {
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
    Gdx.gl.glClearColor(0, 0, 0, 1)
    stage.act(Gdx.graphics.getDeltaTime)
    stage.draw()
  }
  override def pause(): Unit = ()
  override def resume(): Unit = ()
  override def dispose(): Unit = {
    stage.dispose()
  }
}

trait GdxController extends TetricsController[Char, GdxContext] {
  override protected def eventToAction(event: Char): Option[TetricsAction] = {
    event match {
      case 'f' => Some(TurnRightAction)
      case 'd' => Some(TurnLeftAction)
      case 'h' => Some(MoveLeftAction)
      case 'H' => Some(DropAndNormalizeAction(DropLeftAction, NormalizeLeftAction))
      case 'l' => Some(MoveRightAction)
      case 'L' => Some(DropAndNormalizeAction(DropRightAction, NormalizeRightAction))
      case 'k' => Some(MoveUpAction)
      case 'K' => Some(DropAndNormalizeAction(DropTopAction, NormalizeTopAction))
      case 'j' => Some(MoveDownAction)
      case 'J' => Some(DropAndNormalizeAction(DropBottomAction, NormalizeBottomAction))
      case _ => None
    }
    None
  }
}

trait GdxView extends TetricsView[GdxContext] with LabeledFieldView[GdxContext] {
  override def offset: Int = 20
  override def tileWidth: Int = 10
  override def tileHeight: Int = 10
  override def drawField(field: Field, offsetX: Int, offsetY: Int)(implicit ctx: GdxContext): Unit = {

  }
  override def drawLabel(label: Label, offsetX: Int, offsetY: Int)(implicit ctx: GdxContext): Unit = ()
}

trait GdxContext