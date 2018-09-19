package com.yuiwai.tetrics.js

import com.yuiwai.tetrics.core._
import org.scalajs.dom
import org.scalajs.dom.ext.KeyCode
import org.scalajs.dom.html.Canvas
import org.scalajs.dom.raw.{KeyboardEvent, MessageEvent}
import org.scalajs.dom.{CanvasRenderingContext2D => Context2D}

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
    with JsController with JsView with JsCanvasAnimation
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
        ctx.strokeStyle = "rgb(51, 51, 51)"
        if ((row.cols >> x & 1) == 0) {
          ctx.fillStyle = "black"
        } else {
          ctx.fillStyle = "darkgreen"
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
    ctx.fillStyle = "black"
    ctx.rect(offsetX, offsetY, tileWidth * fieldSize, labelHeight)
    ctx.fill()
    ctx.fillStyle = "white"
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
      case dn: DropAndNormalizeAction =>
        tetrics.act(dn.dropAction).field(dn.fieldType).filledRows match {
          case s: Seq[Int] if s.nonEmpty =>
            animation = Some(BlockDeletionAnimation(dn.fieldType, s))
            block(dn.normalizeAction)
            dn.dropAction
          case _ => dn
        }
      case _ => action
    }
  }
  private var animation: Option[Animation[C]] = None
  override def update(delta: Double)(implicit ctx: C, setting: TetricsSetting): Unit = {
    super.update(delta)
    animation match {
      case Some(a) =>
        animation = a.update(delta)
        animation foreach draw
      case None =>
        drawAll(tetrics)
        unblock()
    }
  }
  def draw(animation: Animation[C])(implicit ctx: C): Unit
}
trait JsCanvasAnimation extends AnimationComponent[KeyboardEvent, Context2D] {
  override def draw(animation: Animation[Context2D])(implicit ctx: Context2D): Unit = {
    animation match {
      case b@BlockDeletionAnimation(fieldType, filledRows, _) =>
        drawAll(tetrics)
        ctx.beginPath()
        ctx.fillStyle = s"rgba(255,255,255,${1 - b.rate})"
        fieldType match {
          case FieldLeft =>
            filledRows.foreach { x =>
              ctx.rect(
                offset + tileWidth * (fieldSize - x - 1),
                offset + (tileHeight * (fieldSize + 1)),
                tileWidth,
                tileHeight * fieldSize
              )
            }
          case FieldRight =>
            filledRows.foreach { x =>
              ctx.rect(
                offset + tileWidth * ((fieldSize * 2) + 2 + x),
                offset + (tileHeight * (fieldSize + 1)),
                tileWidth,
                tileHeight * fieldSize
              )
            }
          case FieldTop =>
            filledRows.foreach { y =>
              ctx.rect(
                offset + tileWidth * (fieldSize + 1),
                offset + (tileHeight * (fieldSize - y - 1)),
                tileWidth * fieldSize,
                tileHeight
              )
            }
          case FieldBottom =>
            filledRows.foreach { y =>
              ctx.rect(
                offset + tileWidth * (fieldSize + 1),
                offset + (tileHeight * ((fieldSize * 2) + 2 + y)),
                tileWidth * fieldSize,
                tileHeight
              )
            }
          case _ =>
        }
        ctx.fill()
      case _ =>
    }
  }
}
trait Animation[C] {
  def update(delta: Double): Option[Animation[C]]
}
case class BlockDeletionAnimation[C](fieldType: FieldType, filledRows: Seq[Int], now: Double = 0) extends Animation[C] {
  val long = 400.0
  def rate: Double = now / long
  override def update(delta: Double): Option[Animation[C]] = now + delta match {
    case t if t > long => None
    case t => Some(copy(now = t))
  }
}

trait JsMatchConnector {

}
object JsMatchConnector extends JsMatchConnector