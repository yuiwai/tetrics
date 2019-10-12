package com.yuiwai.tetrics.native

import com.yuiwai.tetrics.app.{Controller, FieldData, Game, Presenter}
import com.yuiwai.tetrics.core._

import scala.scalanative.native._

object NativeApp {
  import Ncurses._
  implicit val setting = DefaultSettings.setting.copy(5, 5)
  val controller = new NativeController
  val presenter = new NativePresenter
  private val game = new NativeGame(presenter)
  def main(args: Array[String]): Unit = {
    val screen: Ptr[Window] = initscr()
    cbreak()
    noecho()
    curs_set(0)
    args.headOption match {
      // TODO: fix auto play
      // case Some("autoPlay") => autoPlay()
      case _ => start()
    }
    endwin()
  }
  def start(): Unit = {
    game.start()
    loop()
  }
  // TODO: fix auto play
  /*
  def autoPlay(): Unit = {
    val autoPlayer = DefaultAutoPlayer()
    game.autoPlay()
    loopAutoPlay(autoPlayer)
  }
  */
  def loop(): Unit = {
    val char = getch
    try {
      controller(game, char)
    } catch {
      case _: IllegalArgumentException =>
      case e => throw e
    }
    loop()
  }
  // TODO: fix auto play
  /*
  def loopAutoPlay(autoPlayer: AutoPlayer): Unit = {
    unistd.usleep(200000.toUInt)
    game.act(autoPlayer)
    game.update(0)
    refresh()
    loopAutoPlay(autoPlayer)
  }
  */
}

final class NativeGame(presenter: Presenter[FieldData])(implicit val setting: TetricsSetting) extends Game {
  private var tetrics = Tetrics(setting.fieldWidth, setting.fieldHeight)
  override def start(): Game = {
    tetrics = draw(randPut(tetrics))
    this
  }
  override def act(action: TetricsAction): Game = {
    tetrics.act(action) match {
      case Left(_) => this
      case Right(r) =>
        action match {
          case _: DropAndNormalizeAction =>
            tetrics = draw(randPut(r.tetrics))
          case _ =>
            tetrics = draw(r.tetrics)
        }
        this
    }
  }
  def draw(tetrics: Tetrics): Tetrics = {
    presenter.draw(FieldTypes.all.map(ft => ft -> FieldData.fromField(tetrics.field(ft))).toMap)
    tetrics
  }
}

final class NativePresenter(implicit setting: TetricsSetting) extends Presenter[FieldData] {
  override def draw(modifiedFields: Map[FieldType, FieldData]): Unit = {
    import setting.{fieldWidth => w, fieldHeight => h}
    val d: (FieldData, CInt, CInt) => Unit = View.drawField(w, h, _, _, _)
    modifiedFields foreach { case (ft, fd) =>
      ft match {
        case FieldLeft => d(fd.rotateRight(h), 0, h + 1)
        case FieldRight => d(fd.rotateLeft(w), (w + 1) * 4, h + 1)
        case FieldTop => d(fd.rotateTwice(w, h), (w + 1) * 2, 0)
        case FieldBottom => d(fd, (w + 1) * 2, (h + 1) * 2)
        case FieldCentral => d(fd, (w + 1) * 2, h + 1)
        case _ =>
      }
    }
  }
}

object View {
  import Ncurses._
  def drawField(fieldWidth: Int, fieldHeight: Int, fieldData: FieldData, offsetX: Int, offsetY: Int): Unit = {
    val pair1 = 1.toShort
    val pair2 = 2.toShort
    start_color()
    init_pair(pair1, Colors.COLOR_BLACK, Colors.COLOR_YELLOW)
    init_pair(pair2, Colors.COLOR_BLACK, Colors.COLOR_RED)
    (0 until fieldHeight).foreach { y =>
      (0 until fieldWidth) foreach { x =>
        if (fieldData(x, y)) {
          attron(COLOR_PAIR(pair2))
          mvprintw(y + offsetY, x * 2 + offsetX, c"  ")
          attroff(COLOR_PAIR(pair2))
        } else {
          attron(COLOR_PAIR(pair1))
          mvprintw(y + offsetY, x * 2 + offsetX, c"  ")
          attroff(COLOR_PAIR(pair1))
        }
      }
    }
  }
}

final class NativeController extends Controller[CInt, Unit] {
  def inputToAction(event: CInt): Option[TetricsAction] = {
    event match {
      // D
      case 100 => Some(TurnLeftAction)
      // F
      case 102 => Some(TurnRightAction)
      // H
      case 104 => Some(MoveLeftAction)
      case 72 => Some(DropAndNormalizeAction(DropLeftAction, NormalizeLeftAction))
      // J
      case 106 => Some(MoveDownAction)
      case 74 => Some(DropAndNormalizeAction(DropBottomAction, NormalizeBottomAction))
      // K
      case 107 => Some(MoveUpAction)
      case 75 => Some(DropAndNormalizeAction(DropTopAction, NormalizeTopAction))
      // L
      case 108 => Some(MoveRightAction)
      case 76 => Some(DropAndNormalizeAction(DropRightAction, NormalizeRightAction))
      // Other
      case _ => None
    }
  }
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
