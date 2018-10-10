package com.yuiwai.tetrics.libgdx
import com.badlogic.gdx.{ApplicationListener, Gdx}
import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.{Actor, InputEvent, InputListener, Stage}
import com.badlogic.gdx.utils.viewport.FitViewport
import com.yuiwai.tetrics.core._

object Main extends Subscriber with DefaultSettings {
  def main(args: Array[String]): Unit = {
    // TODO 定数化
    new LwjglApplication(new AppListener, "tetrics", 520, 520)
  }
}

class AppListener extends ApplicationListener {
  // TODO 定数化
  lazy val stage: Stage = new Stage(new FitViewport(520, 520))
  private var game = _
  override def create(): Unit = {
    Gdx.input.setInputProcessor(stage)
    game = new TenTen with GdxController with GdxView
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

trait GdxController extends TetricsController[InputEvent, GdxContext] {
  override protected def eventToAction(event: InputEvent): Option[TetricsAction] = ???
}

trait GdxView extends TetricsView[GdxContext] {
  override def offset: Int = ???
  override def tileWidth: Int = ???
  override def tileHeight: Int = ???
  override def drawField(field: Field, offsetX: Int, offsetY: Int)(implicit ctx: GdxContext): Unit = ???
}

trait GdxContext