package com.yuiwai.tetrics.native

import com.yuiwai.tetrics.core._

import scala.scalanative.native
import scala.scalanative.native._

object Example {
  import DefaultSettings._
  import Ncurses._
  implicit val ctx: NativeContext = new NativeContext
  private var game = new TenTen[CInt, NativeContext] with NativeView with NativeController
  def main(args: Array[String]): Unit = {
    val screen: Ptr[Window] = initscr()
    cbreak()
    noecho()
    curs_set(0)
    game.start()
    loop()
    endwin()
  }
  def loop(): Unit = {
    val char = getch
    try {
      game.input(char)
    } catch {
      case _: IllegalArgumentException =>
      case e => throw e
    }
    loop()
  }
}

trait NativeView extends LabeledFieldView[NativeContext] {
  import Ncurses._
  override def offset: CInt = 1
  override def tileWidth: CInt = 2
  override def tileHeight: CInt = 1
  def drawField(field: Field, offsetX: Int, offsetY: Int)(implicit ctx: NativeContext): Unit = {
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
  override def drawLabel(label: Label, offsetX: CInt, offsetY: CInt)(implicit ctx: NativeContext): Unit = Zone {
    implicit z =>
      mvprintw(offsetY, offsetX, native.toCString(label.text))
  }
}

trait NativeController extends TetricsController[CInt, NativeContext] {
  override protected def eventToAction(event: CInt): Option[TetricsAction] = {
    event match {
      // D
      case 100 => Some(TurnLeftAction)
      // F
      case 102 => Some(TurnRightAction)
      // H
      case 104 => Some(MoveLeftAction)
      case 72 => Some(DropLeftAction)
      // J
      case 106 => Some(MoveDownAction)
      case 74 => Some(DropBottomAction)
      // K
      case 107 => Some(MoveUpAction)
      case 75 => Some(DropTopAction)
      // L
      case 108 => Some(MoveRightAction)
      case 76 => Some(DropRightAction)
      // Other
      case _ => None
    }
  }
}

class NativeContext

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
