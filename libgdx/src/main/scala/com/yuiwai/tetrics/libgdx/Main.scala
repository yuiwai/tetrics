package com.yuiwai.tetrics.libgdx

import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.{Color, GL20, OrthographicCamera}
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.{Game, Gdx, InputAdapter, Screen}
import com.yuiwai.tetrics.core._

object Main {
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
class MainScreen extends Screen with DefaultSettings {
  implicit val eventBus = EventBus()
  implicit val ctx = new GdxContext
  lazy val shapeRenderer: ShapeRenderer = new ShapeRenderer()
  lazy val camera: OrthographicCamera = new OrthographicCamera()
  lazy val viewport: FitViewport = new FitViewport(520, 520, camera)
  private var game: TetricsGame[Char, GdxContext] = _
  init()
  def init(): Unit = {
    camera.translate(260, 260)
    game = new TenTen[Char, GdxContext] with GdxController with GdxView
    game.start()
    Gdx.input.setInputProcessor(new InputAdapter {
      override def keyTyped(character: Char): Boolean = {
        try {
          game.input(character)
        } catch {
          case _: Throwable => ()
        }
        super.keyTyped(character)
      }
    })
  }
  override def resize(width: Int, height: Int): Unit = viewport.update(width, height)
  override def render(delta: Float): Unit = {
    ctx.clear()
    game.update(delta)

    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
    Gdx.gl.glClearColor(0, 0, 0, 1)

    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
    shapeRenderer.setProjectionMatrix(camera.combined)
    ctx.renderers foreach (_.apply(shapeRenderer))
    shapeRenderer.end()
  }
  override def show(): Unit = ()
  override def hide(): Unit = ()
  override def pause(): Unit = ()
  override def resume(): Unit = ()
  override def dispose(): Unit = {
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
  }
}

trait GdxView extends TetricsView[GdxContext] with LabeledFieldView[GdxContext] {
  override def offset: Int = 20
  override def tileWidth: Int = 15
  override def tileHeight: Int = 15
  override def drawField(field: Field, offsetX: Int, offsetY: Int)(implicit ctx: GdxContext): Unit = {
    ctx.pushRenderer { r =>
      r.setColor(Color.GREEN)
      r.rect(offsetX - 1, 520 - (offsetY + tileHeight * fieldSize + offset - tileHeight) - 1, tileWidth * fieldSize + 1, tileHeight * fieldSize + 1)
      field.rows.zipWithIndex.foreach { case (row, y) =>
        (0 until row.width) foreach { x =>
          if ((row.cols >> x & 1) == 0) {
            r.setColor(Color.BLACK)
          } else {
            r.setColor(Color.RED)
          }
          r.rect(x * tileWidth + offsetX, 520 - (y * tileHeight + offsetY + offset), tileWidth - 1, tileHeight - 1)
        }
      }
    }
  }
  override def drawLabel(label: Label, offsetX: Int, offsetY: Int)(implicit ctx: GdxContext): Unit = ()
}

class GdxContext {
  type Renderer = ShapeRenderer => Unit
  private[tetrics] var renderers: Seq[Renderer] = Seq.empty
  def clear(): Unit = renderers = Seq.empty
  def pushRenderer(renderer: Renderer): Unit = renderers = renderers :+ renderer
}