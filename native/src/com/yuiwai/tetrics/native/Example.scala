package com.yuiwai.tetrics.native

import com.yuiwai.tetrics.core.{Block, Field, Tetrics, TetricsView}

import scala.scalanative.native._

object Example extends TetricsView {
  import Ncurses._
  override def offset: CInt = 1
  override def tileWidth: CInt = 2
  override def tileHeight: CInt = 1
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
  def main(args: Array[String]): Unit = {
    val screen: Ptr[Window] = initscr()
    val win = newwin(10, 10, 1, 1)
    drawAll(tetrics)
    cbreak()
    noecho()
    curs_set(0)
    loop()
    endwin()
  }
  def loop(): Unit = {
    val char = getch
    try {
      char match {
        // D
        case 100 =>
          tetrics = tetrics.turnLeft
        // F
        case 102 =>
          tetrics = tetrics.turnRight
        // H
        case 104 =>
          tetrics = tetrics.moveLeft
          drawCentral(tetrics)
        case 72 =>
          tetrics = randPut(tetrics.dropLeft.normalizeLeft)
          drawLeft(tetrics)
        // J
        case 106 =>
          tetrics = tetrics.moveDown
          drawCentral(tetrics)
        case 74 =>
          tetrics = randPut(tetrics.dropBottom.normalizeBottom)
          drawBottom(tetrics)
        // K
        case 107 =>
          tetrics = tetrics.moveUp
          drawCentral(tetrics)
        case 75 =>
          tetrics = randPut(tetrics.dropTop.normalizeTop)
          drawTop(tetrics)
        // L
        case 108 =>
          tetrics = tetrics.moveRight
          drawCentral(tetrics)
        case 76 =>
          tetrics = randPut(tetrics.dropRight.normalizeRight)
          drawRight(tetrics)
        case _ => ()
      }
      drawAll(tetrics)
    } catch {
      case _: IllegalArgumentException =>
      case e => throw e
    }
    loop()
  }
  def drawField(field: Field, offsetX: Int, offsetY: Int): Unit = {
    val pair1 = 1.toShort
    val pair2 = 2.toShort
    start_color()
    init_pair(pair1, Colors.COLOR_BLACK, Colors.COLOR_YELLOW)
    init_pair(pair2, Colors.COLOR_BLACK, Colors.COLOR_RED)
    field.rows.zipWithIndex.foreach { case (row, y) =>
      (0 until row.width) foreach { x =>
        if ((row.cols >> x & 1) == 0) {
          attron(COLOR_PAIR(pair1))
          mvprintw(y + offsetY, x * 2 + offsetX, c"  ")
          attroff(COLOR_PAIR(pair1))
        } else {
          attron(COLOR_PAIR(pair2))
          mvprintw(y + offsetY, x * 2 + offsetX, c"  ")
          attroff(COLOR_PAIR(pair2))
        }
      }
    }
  }
  def randPut(tetrics: Tetrics): Tetrics = tetrics.putCenter(blocks((Math.random() * blocks.size).toInt))
}

@link("ncurses")
@extern
object Ncurses {
  type Window = CStruct0
  def initscr(): Ptr[Window] = extern
  def newwin(nlines: CInt, ncols: CInt, beginY: CInt, beginX: CInt): Ptr[Window] = extern
  def endwin(): CInt = extern
  def has_colors(): CBool = extern
  def start_color(): CInt = extern
  def init_pair(pair: CShort, f: CShort, b: CShort): CInt = extern
  def COLOR_PAIR(pair: CShort): CInt = extern
  def attron(attribute: CInt): CInt = extern
  def attroff(attribute: CInt): CInt = extern
  def cbreak(): CInt = extern
  def noecho(): CInt = extern
  def getch: CInt = extern
  def mvprintw(y: CInt, x: CInt, fmt: CString, args: CVararg*): CInt = extern
  def mvaddch(y: CInt, x: CInt, ch: CChar): CInt = extern
  def curs_set(visibility: CInt): CInt = extern
}

object Colors {
  val COLOR_BLACK: CShort = 0.toShort
  val COLOR_RED: CShort = 1.toShort
  val COLOR_GREEN: CShort = 2.toShort
  val COLOR_YELLOW: CShort = 3.toShort
  val COLOR_BLUE: CShort = 4.toShort
  val COLOR_MAGENTA: CShort = 5.toShort
  val COLOR_CYAN: CShort = 6.toShort
  val COLOR_WHITE: CShort = 7.toShort
}
