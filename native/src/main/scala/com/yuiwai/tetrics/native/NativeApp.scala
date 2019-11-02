package com.yuiwai.tetrics.native

import com.yuiwai.tetrics.core._
import com.yuiwai.tetrics.native.GameScene.TetricsActionCommand
import com.yuiwai.yachiyo.plain.ApplicationHandler

import scala.scalanative.native._
import com.yuiwai.yachiyo.ui
import com.yuiwai.yachiyo.ui.{Scene, SceneSuite}

object NativeApp extends ui.Application {
  import Ncurses._
  implicit val setting = DefaultSettings.setting.copy(5, 5)
  val GameSceneKey = 2
  override def initialSceneSuiteKey: CInt = GameSceneKey
  def main(args: Array[String]): Unit = {
    val screen: Ptr[Window] = initscr()
    cbreak()
    noecho()
    curs_set(0)
    args.headOption match {
      case _ => start()
    }
    endwin()
  }
  def start(): Unit = {
    ApplicationHandler.run(this)
    loop()
  }
  def loop(): Unit = {
    val char = getch
    try {
      NativeEventHandler
        .inputToAction(char)
        .foreach(action => ApplicationHandler.postCommand(TetricsActionCommand(action).asInstanceOf[Scene#Command]))
    } catch {
      case _: IllegalArgumentException =>
      case e => throw e
    }
    loop()
  }
  override val sceneSuiteMap: Map[Int, SceneSuite] = Map(
    /* TopSceneKey -> SceneSuite(
      () => TopScene,
      () => TopPresenter,
      () => new TopView(broadcaster)
    ), */
    GameSceneKey -> SceneSuite(
      () => new GameScene,
      () => new GamePresenter,
      () => new GameView
    )
  )
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
  def refresh(): CInt = extern
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
