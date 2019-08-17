package com.yuiwai.tetrics.js

import com.yuiwai.tetrics.core._
import org.scalajs.dom
import org.scalajs.dom.ext.KeyCode
import org.scalajs.dom.html.Canvas
import org.scalajs.dom.raw._
import org.scalajs.dom.{CanvasRenderingContext2D => Context2D}

import scala.scalajs.js.typedarray._

object Example extends DefaultSettings {
  self =>
  import dom.window
  import window.document
  private var animationRequestId = 0
  private var semiAuto: Option[SemiAuto] = None
  private var keyDown = false
  private val canvas = document.createElement("canvas")
  private implicit val ctx: Context2D =
    canvas.asInstanceOf[Canvas].getContext("2d").asInstanceOf[Context2D]
  private var game: TetricsGame[UIEvent, Context2D] = _
  private val serializer = new ByteEventSerializer {
    val setting: TetricsSetting = self.setting
  }
  private var lastUpdated = 0.0
  private lazy val updater: Double => Unit = (timestamp: Double) => {
    game.update(timestamp - lastUpdated)
    lastUpdated = timestamp
    animationRequestId = window.requestAnimationFrame(updater)
  }
  def main(args: Array[String]): Unit = {
    reset()
  }
  def reset(): Unit = {
    if (window == window.parent) {
      if (window.screen.width >= 1080) {
        init().start()
      } else {
        initMobile().start()
      }
    } else {
      if (window.top.screen.width >= 1080) {
        initWithParent()
      } else {
        initMobileWithParent()
      }
    }
  }
  def initWithParent(): Unit = initWithParentImpl(false)
  def initMobileWithParent(): Unit = initWithParentImpl(true)
  private def initWithParentImpl(isMobile: Boolean): Unit = {
    val initializer = if (isMobile) initMobile _ else init _
    window.onmessage = (messageEvent: MessageEvent) => {
      if (messageEvent.origin == "https://lab.yuiwai.com") {
        messageEvent.data match {
          case "start" =>
            initializer().start()
          case "readOnly" =>
            initializer().readOnly()
            window.onmessage = handleMessageEvent
          case "autoPlay" =>
            val game = initializer()
            val autoPlayer = DefaultAutoPlayer()
            game.autoPlay()
            dom.window.setInterval(() => game.act(autoPlayer), 250)
          case "semiAuto" =>
            initializer().autoPlay()
          case "semiAutoLoop" =>
            semiAuto match {
              case Some(s) =>
                game.act(s.autoPlayer)
              case None =>
                initializer().autoPlay()
                semiAuto = Some(SemiAuto(DefaultAutoPlayer()))
            }
        }
      }
    }
  }
  def init(): TetricsGame[UIEvent, Context2D] = {
    game = new TenTen[UIEvent, Context2D] with JsController with JsView with JsCanvasAnimation
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
    animationRequestId = window.requestAnimationFrame(updater)
    canvas.setAttribute("width", (game.offset * 2 + game.tileWidth * 32).toString)
    canvas.setAttribute("height", (game.offset * 2 + game.tileHeight * 32).toString)
    game
  }
  def initMobile(): TetricsGame[UIEvent, Context2D] = {
    game = new TenTen[UIEvent, Context2D] with MobileController with JsView with JsCanvasAnimation
    if (animationRequestId == 0) {
      document.body.appendChild(canvas)
      document.body.addEventListener("touchstart", handleTouchEvent)
      document.body.addEventListener("touchmove", handleTouchEvent)
      document.body.addEventListener("touchend", handleTouchEvent)
    }
    animationRequestId = window.requestAnimationFrame(updater)
    canvas.setAttribute("width", window.top.screen.width.toString)
    canvas.setAttribute("height", window.top.screen.width.toString)
    game
  }
  val handleTouchEvent = (e: Event) => game.input(e.asInstanceOf[TouchEvent])
  val handleMessageEvent = (messageEvent: MessageEvent) => {
    // FIXME 暫定readOnlyモード終了処理
    if (messageEvent.data == 0) {
      window.cancelAnimationFrame(animationRequestId)
      game = null
      reset()
    } else game.act(
      serializer.deserialize(int8Array2ByteArray(messageEvent.data.asInstanceOf[Int8Array]))
    )
  }
}

trait JsView extends LabeledFieldView[Context2D] {
  val baseWidth = 520
  val offset = 20
  override val labelHeight = 12
  override val labelMargin = 1
  val tileWidth: Int = 15
  val tileHeight: Int = 15
  def drawField(field: Field, offsetX: Int, offsetY: Int)
    (implicit ctx: Context2D): Unit = {
    val n = ctx.canvas.width.toDouble / 520
    val m = (d: Double) => d * n

    def rect(x: Double, y: Double, w: Double, h: Double) = ctx.rect(m(x), m(y), m(w), m(h))

    field.rows.zipWithIndex.foreach { case (row, y) =>
      (0 until row.width) foreach { x =>
        ctx.beginPath()
        ctx.strokeStyle = "rgb(51, 51, 51)"
        if ((row.cols >> x & 1) == 0) {
          ctx.fillStyle = "black"
        } else {
          ctx.fillStyle = "darkgreen"
        }
        rect(x * tileWidth + offsetX, y * tileHeight + offsetY, tileWidth, tileHeight)
        ctx.fill()
        ctx.stroke()
      }
    }
  }
  def drawLabel(label: Label, offsetX: Int, offsetY: Int)
    (implicit ctx: Context2D): Unit = {
    val n = ctx.canvas.width.toDouble / 520
    val m = (d: Double) => d * n

    def rect(x: Double, y: Double, w: Double, h: Double) = ctx.rect(m(x), m(y), m(w), m(h))

    ctx.beginPath()
    ctx.fillStyle = "black"
    rect(offsetX, offsetY, tileWidth * fieldSize, labelHeight)
    ctx.fill()
    ctx.fillStyle = "white"
    ctx.font = s"${m(12).toInt}px Arial"
    ctx.textAlign = "left"
    ctx.textBaseline = "top"
    ctx.fillText(label.text, m(offsetX), m(offsetY))
  }
}

trait JsController extends TetricsController[UIEvent, Context2D] {
  override protected def eventToAction(e: UIEvent): Option[TetricsAction] = {
    val event = e.asInstanceOf[KeyboardEvent]
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

trait MobileController extends TetricsController[UIEvent, Context2D] {
  private var state = MobileController.State(None, None)
  override def eventToAction(e: UIEvent): Option[TetricsAction] = {
    val event = e.asInstanceOf[TouchEvent]
    val touch = event.changedTouches(0)
    event.`type` match {
      case "touchstart" =>
        if (isLeft(touch)) {
          state = state.copy(left = Some(touch))
        } else {
          state = state.copy(right = Some(touch))
        }
        None
      case "touchend" =>
        state = state.copy(
          state.left.flatMap {
            case t if t.identifier == touch.identifier => None
            case t => Some(t)
          },
          state.right.flatMap {
            case t if t.identifier == touch.identifier => None
            case t => Some(t)
          }
        )
        None
      case "touchmove" =>
        val (a, s) = state.moved(touch)
        state = s
        a
      case _ => None
    }
  }
  def isLeft(e: Touch): Boolean = {
    e.screenX < (dom.window.screen.width / 2)
  }
}
object MobileController {
  // FIXME 暫定値(スクリーンのサイズから算出したい)
  val moveUnit = 25
  val rotateUnit = 35
  case class State(left: Option[Touch], right: Option[Touch]) {
    def moved(touch: Touch): (Option[TetricsAction], State) = {
      import touch.{screenX => x, screenY => y}
      (left, right) match {
        case (None, None) => (None, this)
        case (Some(l), None) if l.identifier == touch.identifier =>
          if (l.screenX > x + moveUnit) {
            (Some(MoveLeftAction), copy(left = Some(touch)))
          } else if (l.screenX < x - moveUnit) {
            (Some(MoveRightAction), copy(left = Some(touch)))
          } else if (l.screenY > y + moveUnit) {
            (Some(MoveUpAction), copy(left = Some(touch)))
          } else if (l.screenY < y - moveUnit) {
            (Some(MoveDownAction), copy(left = Some(touch)))
          } else (None, this)
        case (None, Some(r)) if r.identifier == touch.identifier =>
          if (r.screenX > x + rotateUnit) {
            (Some(TurnLeftAction), copy(right = Some(touch)))
          } else if (r.screenX < x - rotateUnit) {
            (Some(TurnRightAction), copy(right = Some(touch)))
          } else (None, this)
        case (Some(_), Some(r)) if r.identifier == touch.identifier =>
          if (r.screenX > x + moveUnit) {
            (Some(DropAndNormalizeAction(DropLeftAction, NormalizeLeftAction)), copy(right = None))
          } else if (r.screenX < x - moveUnit) {
            (Some(DropAndNormalizeAction(DropRightAction, NormalizeRightAction)), copy(right = None))
          } else if (r.screenY > y + moveUnit) {
            (Some(DropAndNormalizeAction(DropTopAction, NormalizeTopAction)), copy(right = None))
          } else if (r.screenY < y - moveUnit) {
            (Some(DropAndNormalizeAction(DropBottomAction, NormalizeBottomAction)), copy(right = None))
          } else (None, this)
        case _ => (None, this)
      }
    }
  }
  case class Pos(x: Double, y: Double)
}

trait AnimationComponent[E, C] extends TetricsGame[E, C] {
  private var animation: Option[Animation] = None
  def addAnimation(anim: Animation): Unit = {
    animation match {
      case None => animation = Some(anim)
      case Some(a) => animation = Some(a + anim)
    }
  }
  override def beforeAction(action: TetricsAction): TetricsAction = {
    super.beforeAction(action)
    action match {
      case dn: DropAndNormalizeAction =>
        tetrics.act(dn.dropAction).field(dn.fieldType).filledRows match {
          case s: Seq[Int] if s.nonEmpty =>
            addAnimation(BlockDeletionAnimation(dn.fieldType, s))
            block(dn.normalizeAction)
            dn.dropAction
          case _ =>
            addAnimation(DropAnimation(dn.dropAction.fieldType, tetrics.offset, tetrics.block))
            dn
        }
      case mv: MoveAction =>
        addAnimation(BlockMovementAnimation(tetrics.block, tetrics.offset))
        mv
      case rt: RotateAction =>
        addAnimation(BlockRotationAnimation(tetrics.block, tetrics.offset))
        rt
      case _ => action
    }
  }
  override def update(delta: Double)(implicit ctx: C, setting: TetricsSetting): Unit = {
    drawAll(tetrics)
    animation match {
      case Some(a) =>
        animation = a.update(delta)
        animation foreach { anim =>
          if (!anim.isBlocking) unblock()
          draw(anim)
        }
      case None => unblock()
    }
  }
  def draw(animation: Animation)(implicit ctx: C): Unit
}
trait JsCanvasAnimation extends AnimationComponent[UIEvent, Context2D] {
  override def draw(animation: Animation)(implicit ctx: Context2D): Unit = {
    val n = ctx.canvas.width.toDouble / 520
    val m = (d: Double) => d * n

    def rect(x: Double, y: Double, w: Double, h: Double) = ctx.rect(m(x), m(y), m(w), m(h))

    animation match {
      case CompositeAnimation(animations) => animations foreach draw
      case d@DropAnimation(fieldType, o, b, _) =>
        ctx.beginPath()
        ctx.fillStyle = s"rgba(0,0,127,${1 - d.rate})"
        fieldType match {
          case FieldLeft =>
            rect(
              offset,
              offset + (tileHeight * (fieldSize + 1 + o.y)),
              tileWidth * fieldSize * d.rate,
              tileHeight * b.height
            )
          case FieldRight =>
            rect(
              offset + tileWidth * (fieldSize * 3 + 2) - tileWidth * fieldSize * d.rate,
              offset + (tileHeight * (fieldSize + 1 + o.y)),
              tileWidth * fieldSize * d.rate,
              tileHeight * b.height
            )
          case FieldTop =>
            rect(
              offset + tileWidth * (fieldSize + 1 + o.x),
              offset,
              tileWidth * b.width,
              tileHeight * fieldSize * d.rate
            )
          case FieldBottom =>
            rect(
              offset + tileWidth * (fieldSize + 1 + o.x),
              offset + tileHeight * (fieldSize * 3 + 2) - tileHeight * fieldSize * d.rate,
              tileWidth * b.width,
              tileHeight * fieldSize * d.rate
            )
          case _ =>
        }
        ctx.fill()
      case b@BlockDeletionAnimation(fieldType, filledRows, _) =>
        ctx.beginPath()
        ctx.fillStyle = s"rgba(255,255,255,${1 - b.rate})"
        fieldType match {
          case FieldLeft =>
            filledRows.foreach { x =>
              rect(
                offset + tileWidth * (fieldSize - x - 1),
                offset + (tileHeight * (fieldSize + 1)),
                tileWidth,
                tileHeight * fieldSize
              )
            }
          case FieldRight =>
            filledRows.foreach { x =>
              rect(
                offset + tileWidth * ((fieldSize * 2) + 2 + x),
                offset + (tileHeight * (fieldSize + 1)),
                tileWidth,
                tileHeight * fieldSize
              )
            }
          case FieldTop =>
            filledRows.foreach { y =>
              rect(
                offset + tileWidth * (fieldSize + 1),
                offset + (tileHeight * (fieldSize - y - 1)),
                tileWidth * fieldSize,
                tileHeight
              )
            }
          case FieldBottom =>
            filledRows.foreach { y =>
              rect(
                offset + tileWidth * (fieldSize + 1),
                offset + (tileHeight * ((fieldSize * 2) + 2 + y)),
                tileWidth * fieldSize,
                tileHeight
              )
            }
          case _ =>
        }
        ctx.fill()
      case b@BlockMovementAnimation(block, o, _) =>
        ctx.beginPath()
        ctx.strokeStyle = s"rgba(0,127,0,${1 - b.rate})"
        block.rows.zipWithIndex.foreach { case (row, y) =>
          (0 until row.width) foreach { x =>
            if ((row.cols >> x & 1) == 1)
              rect(
                offset + tileWidth * (fieldSize + 1 + o.x + x),
                offset + tileHeight * (fieldSize + 1 + o.y + y),
                tileWidth,
                tileHeight
              )
          }
        }
        ctx.stroke()
      case b@BlockRotationAnimation(block, o, _) =>
        ctx.beginPath()
        ctx.strokeStyle = s"rgba(0,127,0,${1 - b.rate})"
        block.rows.zipWithIndex.foreach { case (row, y) =>
          (0 until row.width) foreach { x =>
            if ((row.cols >> x & 1) == 1)
              rect(
                offset + tileWidth * (fieldSize + 1 + o.x + x),
                offset + tileHeight * (fieldSize + 1 + o.y + y),
                tileWidth,
                tileHeight
              )
          }
        }
        ctx.stroke()
      case _ =>
    }
  }
}
trait Animation {
  val long: Double
  val now: Double
  def +(that: Animation): CompositeAnimation = CompositeAnimation(Seq(this, that))
  def rate: Double = now / long
  def update(delta: Double): Option[Animation]
  def isBlocking: Boolean = false
}
trait BlockingAnimation extends Animation {
  override def isBlocking: Boolean = true
}
case class CompositeAnimation(animations: Seq[Animation]) extends Animation {
  val long: Double = 0
  val now: Double = 0
  override def +(that: Animation): CompositeAnimation = copy(animations :+ that)
  override def update(delta: Double): Option[Animation] = {
    animations.map(_.update(delta)).filter(_.isDefined).flatten match {
      case s if s.nonEmpty => Some(copy(s))
      case _ => None
    }
  }
  override def isBlocking: Boolean = animations.exists(_.isBlocking)
}
case class DropAnimation(fieldType: FieldType, offset: Offset, block: Block, now: Double = 0) extends Animation {
  val long = 400.0
  override def update(delta: Double): Option[Animation] = now + delta match {
    case t if t > long => None
    case t => Some(copy(now = t))
  }
}
case class BlockDeletionAnimation(fieldType: FieldType, filledRows: Seq[Int], now: Double = 0)
  extends Animation with BlockingAnimation {
  val long = 400.0
  override def update(delta: Double): Option[Animation] = now + delta match {
    case t if t > long => None
    case t => Some(copy(now = t))
  }
}
case class BlockMovementAnimation(block: Block, offset: Offset, now: Double = 0) extends Animation {
  val long = 300.0
  override def update(delta: Double): Option[Animation] = now + delta match {
    case t if t > long => None
    case t => Some(copy(now = t))
  }
}
case class BlockRotationAnimation(block: Block, offset: Offset, now: Double = 0) extends Animation {
  val long = 300.0
  override def update(delta: Double): Option[Animation] = now + delta match {
    case t if t > long => None
    case t => Some(copy(now = t))
  }
}

trait JsMatchConnector {

}
object JsMatchConnector extends JsMatchConnector

case class SemiAuto(autoPlayer: AutoPlayer)