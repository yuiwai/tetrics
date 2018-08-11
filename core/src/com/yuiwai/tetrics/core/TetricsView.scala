package com.yuiwai.tetrics.core

trait TetricsView {
  def offset: Int
  def tileWidth: Int
  def tileHeight: Int
  def fieldSize: Int = 10
  def drawAll(tetrics: Tetrics): Unit = {
    drawTop(tetrics)
    drawLeft(tetrics)
    drawCentral(tetrics)
    drawRight(tetrics)
    drawBottom(tetrics)
  }
  def drawTop(tetrics: Tetrics): Unit =
    drawField(tetrics.topField.turnLeft.turnLeft, offset + (tileWidth * (fieldSize + 1)), offset)
  def drawLeft(tetrics: Tetrics): Unit =
    drawField(tetrics.leftField.turnRight, offset, offset + (tileHeight * (fieldSize + 1)))
  def drawCentral(tetrics: Tetrics): Unit =
    drawField(tetrics.centralField, offset + (tileWidth * (fieldSize + 1)), offset + (tileHeight * (fieldSize + 1)))
  def drawRight(tetrics: Tetrics): Unit =
    drawField(tetrics.rightField.turnLeft, offset + (tileWidth * (fieldSize + 1) * 2), offset + (tileHeight * (fieldSize + 1)))
  def drawBottom(tetrics: Tetrics): Unit =
    drawField(tetrics.bottomField, offset + (tileWidth * (fieldSize + 1)), offset + (tileHeight * (fieldSize + 1) * 2))
  def drawField(field: Field, offsetX: Int, offsetY: Int): Unit
}
