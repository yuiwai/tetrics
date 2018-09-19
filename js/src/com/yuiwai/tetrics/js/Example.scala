package com.yuiwai.tetrics.js

import com.yuiwai.tetrics.core._
import org.scalajs.dom
import org.scalajs.dom.ext.KeyCode
import org.scalajs.dom.html.Canvas
import org.scalajs.dom.raw.{KeyboardEvent, MessageEvent}
import org.scalajs.dom.{CanvasRenderingContext2D => Context2D}

import scala.scalajs.js.Date
import scala.scalajs.js.typedarray._

object Example extends Subscriber with DefaultSettings {
  self =>
  import dom.window
  import window.document
  private var keyDown = false
  private val canvas = document.createElement("canvas")
  private implicit val eventBus = EventBus()
  private implicit val ctx: Context2D =
    canvas.asInstanceOf[Canvas].getContext("2d").asInstanceOf[Context2D]
  private var game = new TenTen[KeyboardEvent, Context2D]
    with JsController with JsView with AnimationComponent[KeyboardEvent, Context2D]
  private val serializer = new ByteEventSerializer {
    val setting: TetricsSetting = self.setting
  }
  def main(args: Array[String]): Unit = {
    if (window == window.parent) {
      init().start()
    } else {
      initWithParent()
    }
  }
  def initWithParent(): Unit = {
    window.onmessage = (messageEvent: MessageEvent) => {
      if (messageEvent.origin == "https://lab.yuiwai.com") {
        messageEvent.data match {
          case "start" =>
            subscribe(gameEvent => {
              messageEvent.source.postMessage(
                byteArray2Int8Array(serializer.serialize(gameEvent)),
                messageEvent.origin
              )
            })
            init().start()
          case "readOnly" =>
            init().readOnly()
            window.onmessage = handleMessageEvent
          case "autoPlay" =>
            val game = init()
            val autoPlayer = DefaultAutoPlayer()
            game.autoPlay()
            dom.window.setInterval(() => game.act(autoPlayer), 250)
        }
      }
    }
  }
  def init(): TetricsGame[KeyboardEvent, Context2D] = {
    var lastUpdated = .0
    lazy val updater: Double => Unit = (timestamp: Double) => {
      game.update(timestamp - lastUpdated)
      lastUpdated = timestamp
      window.requestAnimationFrame(updater)
    }
    document.body.appendChild(canvas)
    window.onkeydown = (e: KeyboardEvent) => {
      if (!keyDown) {
        keyDown = true
        e.keyCode match {
          case KeyCode.Shift =>
            keyDown = false
          case _ => game.input(e)
        }
      }
      window.onkeyup = (_: KeyboardEvent) => {
        keyDown = false
      }
    }
    window.requestAnimationFrame(updater)
    canvas.setAttribute("width", (game.offset * 2 + game.tileWidth * 32).toString)
    canvas.setAttribute("height", (game.offset * 2 + game.tileHeight * 32).toString)
    game
  }
  val handleMessageEvent = (messageEvent: MessageEvent) => {
    game.act(
      serializer.deserialize(int8Array2ByteArray(messageEvent.data.asInstanceOf[Int8Array]))
    )
  }
}

trait JsView extends LabeledFieldView[Context2D] {
  val offset = 20
  override val labelHeight = 12
  override val labelMargin = 1
  val tileWidth: Int = 15
  val tileHeight: Int = 15
  def drawField(field: Field, offsetX: Int, offsetY: Int)
    (implicit ctx: Context2D): Unit = {
    field.rows.zipWithIndex.foreach { case (row, y) =>
      (0 until row.width) foreach { x =>
        ctx.beginPath()
        if ((row.cols >> x & 1) == 0) {
          ctx.fillStyle = "yellow"
          ctx.strokeStyle = "grey"
        } else {
          ctx.fillStyle = "red"
          ctx.strokeStyle = "grey"
        }
        ctx.rect(x * tileWidth + offsetX, y * tileHeight + offsetY, tileWidth, tileHeight)
        ctx.fill()
        ctx.stroke()
      }
    }
  }
  def drawLabel(label: Label, offsetX: Int, offsetY: Int)
    (implicit ctx: Context2D): Unit = {
    ctx.beginPath()
    ctx.fillStyle = "white"
    ctx.rect(offsetX, offsetY, tileWidth * fieldSize, labelHeight)
    ctx.fill()
    ctx.fillStyle = "black"
    ctx.font = "12px Arial"
    ctx.textAlign = "left"
    ctx.textBaseline = "top"
    ctx.fillText(label.text, offsetX, offsetY)
  }
}

trait JsController extends TetricsController[KeyboardEvent, Context2D] {
  override protected def eventToAction(event: KeyboardEvent): Option[TetricsAction] = {
    event.keyCode match {
      case KeyCode.F => Some(TurnRightAction)
      case KeyCode.D => Some(TurnLeftAction)
      case KeyCode.Left | KeyCode.H =>
        if (event.shiftKey) Some(DropAndNormalizeAction(DropLeftAction, NormalizeLeftAction))
        else Some(MoveLeftAction)
      case KeyCode.Right | KeyCode.L =>
        if (event.shiftKey) Some(DropAndNormalizeAction(DropRightAction, NormalizeRightAction))
        else Some(MoveRightAction)
      case KeyCode.Up | KeyCode.K =>
        if (event.shiftKey) Some(DropAndNormalizeAction(DropTopAction, NormalizeTopAction))
        else Some(MoveUpAction)
      case KeyCode.Down | KeyCode.J =>
        if (event.shiftKey) Some(DropAndNormalizeAction(DropBottomAction, NormalizeBottomAction))
        else Some(MoveDownAction)
      case _ => None
    }
  }
}

trait AnimationComponent[E, C] extends TetricsGame[E, C] {
  override def beforeAction(action: TetricsAction): TetricsAction = {
    super.beforeAction(action)
    action match {
      case n: NormalizeAction =>
        tetrics.field(n.fieldType).filledRows match {
          case s: Seq[Int] if s.nonEmpty =>
            block(n)
            NoAction
          case _ => action
        }
      case _ => action
    }
  }
  private var animation: Option[Animation[C]] = None
  override def update(delta: Double)(implicit ctx: C, setting: TetricsSetting): Unit = {
    super.update(delta)
    // FIXME 暫定対応
    unblock()
    // animation foreach()
  }
  def draw()(implicit ctx: C): Unit = {
  }
}
trait Animation[C]
trait BlockingAnimation[C] extends Animation[C]

trait JsMatchConnector {

}
object JsMatchConnector extends JsMatchConnector