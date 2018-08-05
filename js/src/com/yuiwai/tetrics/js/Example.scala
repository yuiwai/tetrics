package com.yuiwai.tetrics.js

import com.yuiwai.tetrics.core.{Block, Field, Tetrics}
import org.scalajs.dom
import org.scalajs.dom.CanvasRenderingContext2D
import org.scalajs.dom.ext.KeyCode
import org.scalajs.dom.html.Canvas
import org.scalajs.dom.raw.KeyboardEvent

object Example {
  val offset = 20
  val tile = 15
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
  canvas.setAttribute("width", (offset * 2 + tile * 32).toString)
  canvas.setAttribute("height", (offset * 2 + tile * 32).toString)
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
              tetrics = randPut(tetrics.dropRight.normalized)
              drawRight()
            }
            else tetrics = tetrics.moveRight
          case KeyCode.Left | KeyCode.H =>
            if (e.shiftKey) {
              tetrics = randPut(tetrics.dropLeft.normalized)
              drawLeft()
            }
            else tetrics = tetrics.moveLeft
          case KeyCode.Up | KeyCode.K =>
            if (e.shiftKey) {
              tetrics = randPut(tetrics.dropTop.normalized)
              drawTop()
            }
            else tetrics = tetrics.moveUp
          case KeyCode.Down | KeyCode.J =>
            if (e.shiftKey) {
              tetrics = randPut(tetrics.dropBottom.normalized)
              drawBottom()
            }
            else tetrics = tetrics.moveDown
          case _ =>
        }
        drawCentral()
      }
    }
    dom.window.onkeyup = (_: KeyboardEvent) => {
      keyDown = false
    }
    drawAll()
  }
  def drawAll(): Unit = {
    drawTop()
    drawLeft()
    drawCentral()
    drawRight()
    drawBottom()
  }
  def drawTop(): Unit = draw(tetrics.topField.turnLeft.turnLeft, offset + (tile * 11), offset)
  def drawLeft(): Unit = draw(tetrics.leftField.turnRight, offset, offset + (tile * 11))
  def drawCentral(): Unit = draw(tetrics.centralField, offset + (tile * 11), offset + (tile * 11))
  def drawRight(): Unit = draw(tetrics.rightField.turnLeft, offset + (tile * 22), offset + (tile * 11))
  def drawBottom(): Unit = draw(tetrics.bottomField, offset + (tile * 11), offset + (tile * 22))
  def draw(field: Field, offsetX: Int, offsetY: Int): Unit = {
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
        ctx.rect(x * tile + offsetX, y * tile + offsetY, tile, tile)
        ctx.fill()
        ctx.stroke()
      }
    }
  }
  def randPut(tetrics: Tetrics): Tetrics = tetrics.putCenter(blocks((Math.random() * blocks.size).toInt))
}
