package com.yuiwai.tetrics.js

import com.yuiwai.tetrics.core._
import org.scalajs.dom
import org.scalajs.dom.CanvasRenderingContext2D
import org.scalajs.dom.ext.KeyCode
import org.scalajs.dom.html.Canvas
import org.scalajs.dom.raw.KeyboardEvent

object Example {
  import DefaultSettings._
  private var keyDown = false
  private val canvas = dom.window.document.createElement("canvas")
  private implicit val ctx: CanvasRenderingContext2D =
    canvas.asInstanceOf[Canvas].getContext("2d").asInstanceOf[CanvasRenderingContext2D]
  private var game = new TenTen[KeyboardEvent, CanvasRenderingContext2D] with JsController with JsView
  def main(args: Array[String]): Unit = {
    dom.window.document.body.appendChild(canvas)
    dom.window.onkeydown = (e: KeyboardEvent) => {
      if (!keyDown) {
        keyDown = true
        e.keyCode match {
          case KeyCode.Shift =>
            keyDown = false
          case _ => game.input(e)
        }
      }
      dom.window.onkeyup = (_: KeyboardEvent) => {
        keyDown = false
      }
    }
    canvas.setAttribute("width", (game.offset * 2 + game.tileWidth * 32).toString)
    canvas.setAttribute("height", (game.offset * 2 + game.tileHeight * 32).toString)
    game.start()
  }
}

trait JsView extends LabeledFieldView[CanvasRenderingContext2D] {
  val offset = 20
  override val labelHeight = 12
  override val labelMargin = 1
  val tileWidth: Int = 15
  val tileHeight: Int = 15
  def drawField(field: Field, offsetX: Int, offsetY: Int)
    (implicit ctx: CanvasRenderingContext2D): Unit = {
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
    (implicit ctx: CanvasRenderingContext2D): Unit = {
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

trait JsController extends TetricsController[KeyboardEvent, CanvasRenderingContext2D] {
  override protected def eventToAction(event: KeyboardEvent): Option[TetricsAction] = {
    event.keyCode match {
      case KeyCode.F => Some(TurnRightAction)
      case KeyCode.D => Some(TurnLeftAction)
      case KeyCode.Right | KeyCode.L =>
        if (event.shiftKey) Some(DropRightAction)
        else Some(MoveRightAction)
      case KeyCode.Left | KeyCode.H =>
        if (event.shiftKey) Some(DropLeftAction)
        else Some(MoveLeftAction)
      case KeyCode.Up | KeyCode.K =>
        if (event.shiftKey) Some(DropTopAction)
        else Some(MoveUpAction)
      case KeyCode.Down | KeyCode.J =>
        if (event.shiftKey) Some(DropBottomAction)
        else Some(MoveDownAction)
      case _ => None
    }
  }
}
