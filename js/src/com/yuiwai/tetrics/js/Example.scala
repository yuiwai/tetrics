package com.yuiwai.tetrics.js

import com.yuiwai.tetrics.core.{Block, Field, Tetrics, TetricsView}
import org.scalajs.dom
import org.scalajs.dom.CanvasRenderingContext2D
import org.scalajs.dom.ext.KeyCode
import org.scalajs.dom.html.Canvas
import org.scalajs.dom.raw.KeyboardEvent

object Example extends TetricsView {
  val offset = 20
  val tileWidth: Int = 15
  val tileHeight: Int = 15
  private var keyDown = false
  private val canvas = dom.window.document.createElement("canvas")
  private val blocks = Seq(
    Block("1111", 4),
    Block("1111", 2),
    Block("010111", 3),
    Block("001111", 3),
    Block("100111", 3),
    Block("110011", 3),
    Block("011110", 3)
  )
  private var tetrics = randPut(Tetrics(10))
  canvas.setAttribute("width", (offset * 2 + tileWidth * 32).toString)
  canvas.setAttribute("height", (offset * 2 + tileHeight * 32).toString)
  private val ctx = canvas.asInstanceOf[Canvas].getContext("2d").asInstanceOf[CanvasRenderingContext2D]
  dom.window.document.body.appendChild(canvas)
  def main(args: Array[String]): Unit = {
    dom.window.onkeydown = (e: KeyboardEvent) => {
      if (!keyDown) {
        keyDown = true
        e.keyCode match {
          case KeyCode.Shift =>
            keyDown = false
          case KeyCode.F =>
            tetrics = tetrics.turnRight
          case KeyCode.D =>
            tetrics = tetrics.turnLeft
          case KeyCode.Right | KeyCode.L =>
            if (e.shiftKey) {
              tetrics = randPut(tetrics.dropRight.normalizeRight)
              drawRight(tetrics)
            }
            else tetrics = tetrics.moveRight
          case KeyCode.Left | KeyCode.H =>
            if (e.shiftKey) {
              tetrics = randPut(tetrics.dropLeft.normalizeLeft)
              drawLeft(tetrics)
            }
            else tetrics = tetrics.moveLeft
          case KeyCode.Up | KeyCode.K =>
            if (e.shiftKey) {
              tetrics = randPut(tetrics.dropTop.normalizeTop)
              drawTop(tetrics)
            }
            else tetrics = tetrics.moveUp
          case KeyCode.Down | KeyCode.J =>
            if (e.shiftKey) {
              tetrics = randPut(tetrics.dropBottom.normalizeBottom)
              drawBottom(tetrics)
            }
            else tetrics = tetrics.moveDown
          case _ =>
        }
        drawCentral(tetrics)
      }
    }
    dom.window.onkeyup = (_: KeyboardEvent) => {
      keyDown = false
    }
    drawAll(tetrics)
  }
  def drawField(field: Field, offsetX: Int, offsetY: Int): Unit = {
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
  def randPut(tetrics: Tetrics): Tetrics = tetrics.putCenter(blocks((Math.random() * blocks.size).toInt))
}
