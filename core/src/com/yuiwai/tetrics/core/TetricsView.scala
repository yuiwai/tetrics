package com.yuiwai.tetrics.core

trait TetricsView {
  def offset: Int
  def tileWidth: Int
  def tileHeight: Int
  def fieldSize: Int = 10
  def drawAll(tetrics: Tetrics): Unit = {
    drawCentral(tetrics)
  }
  def drawCentral(tetrics: Tetrics): Unit =
    drawField(tetrics.centralField, offset + (tileWidth * (fieldSize + 1)), offset + (tileHeight * (fieldSize + 1)))
  def drawField(field: Field, offsetX: Int, offsetY: Int): Unit
}
