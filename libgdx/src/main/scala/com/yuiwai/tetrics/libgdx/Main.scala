package com.yuiwai.tetrics.libgdx

import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.{Color, GL20}
import com.badlogic.gdx.scenes.scene2d.{InputEvent, InputListener, Stage}
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.{Game, Gdx, Screen}
import com.yuiwai.tetrics.core._

import scala.collection.mutable

object Main extends Subscriber with DefaultSettings {
  def main(args: Array[String]): Unit = {
    // TODO 定数化
    new LwjglApplication(new AppListener, "tetrics", 520, 520)
  }
}

class AppListener extends Game {
  override def create(): Unit = {
    setScreen(new MainScreen)
  }
}
class MainScreen extends Screen with DefaultSettings  {
  implicit val eventBus = EventBus()
  implicit val ctx = new GdxContext
  // TODO 定数化
  lazy val stage: Stage = new Stage(new FitViewport(520, 520))
  lazy val shapeRenderer: ShapeRenderer = new ShapeRenderer()
  private var game: TetricsGame[Char, GdxContext] = _
  init()
  def init(): Unit = {
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
    // stage.getViewport.update(width, height)
  }
  override def render(delta: Float): Unit = {
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
    Gdx.gl.glClearColor(0, 0, 0, 1)

    shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
    shapeRenderer.setColor(Color.GREEN)
    draw()
    shapeRenderer.end()
    // stage.act(Gdx.graphics.getDeltaTime)
    // stage.draw()
  }
  private def draw(): Unit = {
    ctx.popRenderer() match {
      case Some(renderer) =>
        renderer(shapeRenderer)
        draw()
      case None =>
    }
  }
  override def show(): Unit = ()
  override def hide(): Unit = ()
  override def pause(): Unit = ()
  override def resume(): Unit = ()
  override def dispose(): Unit = {
    stage.dispose()
    shapeRenderer.dispose()
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
    ctx.pushRenderer { r =>
      r.rect(offsetX, offsetY, tileWidth, tileHeight)
    }
  }
  override def drawLabel(label: Label, offsetX: Int, offsetY: Int)(implicit ctx: GdxContext): Unit = ()
}

class GdxContext {
  type Renderer = ShapeRenderer => Unit
  private val renderers: mutable.Queue[Renderer] = mutable.Queue.empty
  def popRenderer(): Option[Renderer] = if (renderers.isEmpty) None else Some(renderers.dequeue())
  def pushRenderer(renderer: Renderer): Unit = renderers.enqueue(renderer)
}