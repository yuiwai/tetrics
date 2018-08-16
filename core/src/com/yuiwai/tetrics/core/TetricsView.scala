package com.yuiwai.tetrics.core

trait TetricsView[C] {
  def offset: Int
  def tileWidth: Int
  def tileHeight: Int
  def fieldSize: Int = 10
  def drawAll(tetrics: Tetrics)(implicit ctx: C): Unit = {
    drawTop(tetrics)
    drawLeft(tetrics)
    drawCentral(tetrics)
    drawRight(tetrics)
    drawBottom(tetrics)
  }
  def drawTop(tetrics: Tetrics)(implicit ctx: C): Unit =
    drawField(tetrics.topField.turnLeft.turnLeft, offset + (tileWidth * (fieldSize + 1)), offset)
  def drawLeft(tetrics: Tetrics)(implicit ctx: C): Unit =
    drawField(tetrics.leftField.turnRight, offset, offset + (tileHeight * (fieldSize + 1)))
  def drawCentral(tetrics: Tetrics)(implicit ctx: C): Unit =
    drawField(tetrics.centralField, offset + (tileWidth * (fieldSize + 1)), offset + (tileHeight * (fieldSize + 1)))
  def drawRight(tetrics: Tetrics)(implicit ctx: C): Unit =
    drawField(tetrics.rightField.turnLeft, offset + (tileWidth * (fieldSize + 1) * 2), offset + (tileHeight * (fieldSize + 1)))
  def drawBottom(tetrics: Tetrics)(implicit ctx: C): Unit =
    drawField(tetrics.bottomField, offset + (tileWidth * (fieldSize + 1)), offset + (tileHeight * (fieldSize + 1) * 2))
  def drawField(field: Field, offsetX: Int, offsetY: Int)(implicit ctx: C): Unit
}
trait LabeledFieldView[C] extends TetricsView[C] {
  def labelHeight: Int = offset
  def labelMargin: Int = 0
  def drawLeftLabel(label: Label)(implicit ctx: C): Unit =
    drawLabel(label, offset, offset + tileHeight * (fieldSize + 1) - labelHeight - labelMargin)
  def drawRightLabel(label: Label)(implicit ctx: C): Unit =
    drawLabel(label, offset + tileWidth * (fieldSize + 1) * 2, offset + tileHeight * (fieldSize + 1) - labelHeight - labelMargin)
  def drawTopLabel(label: Label)(implicit ctx: C): Unit =
    drawLabel(label, offset + (tileWidth * (fieldSize + 1)), offset - labelHeight - labelMargin)
  def drawBottomLabel(label: Label)(implicit ctx: C): Unit =
    drawLabel(label, offset + (tileWidth * (fieldSize + 1)), offset + tileHeight * ((fieldSize + 1) * 2 + fieldSize) + labelMargin)
  def drawLabel(label: Label, offsetX: Int, offsetY: Int)(implicit ctx: C): Unit
}
case class Label(text: String)
