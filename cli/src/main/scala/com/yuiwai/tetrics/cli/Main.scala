package com.yuiwai.tetrics.cli

import com.yuiwai.tetrics.app.{Controller, Game, Pos, Presenter}
import com.yuiwai.tetrics.core._

import scala.io.StdIn

object Main extends DefaultSettings {
  val controller = new CliController
  def main(args: Array[String]): Unit = {
    val presenter = new CliPresenter
    val game = new CliGame(presenter).start()

    loop(Some(game))
  }
  def loop(gameOpt: Option[Game]): Unit = {
    gameOpt match {
      case None => ()
      case Some(game) =>
        val cmd = StdIn.readLine()
        loop(
          cmd.foldLeft[Option[Game]](Some(game)) {
            case (_, '$') => None
            case (None, _) => None
            case (Some(g), c) =>
              controller.inputToAction(c) match {
                case Some(a) => Some(g.act(a))
                case _ => Some(g)
              }
          }
        )
    }
  }
}
final class CliGame(presenter: CliPresenter)(implicit val setting: TetricsSetting) extends Game {
  implicit val eventBus: EventBus = EventBus()
  private var tetrics = Tetrics()
  override def start(): Game = {
    tetrics = draw(randPut(tetrics))
    this
  }
  override def act(action: TetricsAction): Game = {
    action match {
      case _: DropAndNormalizeAction =>
        tetrics = draw(randPut(tetrics.act(action)))
      case _ =>
        tetrics = draw(tetrics.act(action))
    }
    this
  }
  def draw(tetrics: Tetrics): Tetrics = {
    presenter.draw(FieldTypes.all.map(ft => ft -> FieldData.fromField(tetrics.field(ft))).toMap)
    tetrics
  }
}
final class CliController extends Controller[Char, Unit] {
  override def inputToAction(input: Char): Option[TetricsAction] = input match {
    case 'd' => Some(TurnLeftAction)
    case 'f' => Some(TurnRightAction)
    case 'h' => Some(MoveLeftAction)
    case 'l' => Some(MoveRightAction)
    case 'k' => Some(MoveUpAction)
    case 'j' => Some(MoveDownAction)
    case 'H' => Some(DropAndNormalizeAction(DropLeftAction, NormalizeLeftAction))
    case 'L' => Some(DropAndNormalizeAction(DropRightAction, NormalizeRightAction))
    case 'K' => Some(DropAndNormalizeAction(DropTopAction, NormalizeTopAction))
    case 'J' => Some(DropAndNormalizeAction(DropBottomAction, NormalizeBottomAction))
    case _ => None
  }
}
final class CliPresenter extends Presenter[FieldData] {
  private var fields: Map[FieldType, FieldData] = FieldTypes.all.map(_ -> FieldData.empty).toMap
  override def draw(modifiedFields: Map[FieldType, FieldData]): Unit = {
    modifiedFields.foreach(t => fields = fields.updated(t._1, t._2))
    // FIXME とりあえずフィールドサイズは固定値
    View.render(10, 10, fields)
  }
}
final case class FieldData(filled: Set[Pos]) {
  def map(f: Pos => Pos): FieldData = FieldData(filled.map(f))
  def apply(x: Int, y: Int): Boolean = apply(Pos(x, y))
  def apply(pos: Pos): Boolean = filled(pos)
  def rotateRight(fieldHeight: Int): FieldData = map(pos => Pos(fieldHeight - pos.y - 1, pos.x))
  def rotateLeft(fieldWidth: Int): FieldData = map(pos => Pos(pos.y, fieldWidth - pos.x - 1))
  def rotateTwice(fieldWidth: Int, fieldHeight: Int): FieldData =
    map(pos => Pos(fieldWidth - pos.x - 1, fieldHeight - pos.y - 1))
}
object FieldData {
  def empty: FieldData = FieldData(Set.empty)
  def fromField(field: Field): FieldData = FieldData {
    (for {
      y <- 0 until field.height
      x <- 0 until field.rows(y).width
      if (field.rows(y).cols & 1 << x) != 0
    } yield Pos(x, y)).toSet
  }
}
object View {
  def render(fieldWidth: Int, fieldHeight: Int, fields: Map[FieldType, FieldData]): Unit = {
    val e = emptyField(fieldWidth, fieldHeight)
    val p = emptyField(1, fieldHeight)
    val rf = (ft: FieldType) => renderField(fieldWidth, fieldHeight, ft, fields(ft))
    val display = Seq(
      concat(e, p, rf(FieldTop)),
      concat(rf(FieldLeft), p, rf(FieldCentral), p, rf(FieldRight)),
      concat(e, p, rf(FieldBottom))
    )
    println(display.map(_.mkString("\n")).mkString("\n\n") + "\n")
  }
  def concat(left: Seq[String], right: Seq[String], rest: Seq[String]*): Seq[String] = {
    val r = left.zip(right).map(ss => ss._1 + ss._2)
    if (rest.isEmpty) r
    else concat(r, rest.head, rest.tail: _*)
  }
  def renderField(fieldWidth: Int, fieldHeight: Int, fieldType: FieldType, fieldData: FieldData): Seq[String] = {
    val fd = fieldType match {
      case FieldLeft => fieldData.rotateRight(fieldHeight)
      case FieldRight => fieldData.rotateLeft(fieldWidth)
      case FieldTop => fieldData.rotateTwice(fieldWidth, fieldHeight)
      case _ => fieldData
    }
    (0 until fieldHeight).foldLeft(Seq.empty[String]) { case (a, y) =>
      a :+ (0 until fieldWidth).foldLeft("") { case (b, x) =>
        b + (if (fd(x, y)) "*" else "-")
      }
    }
  }
  def emptyField(fieldWidth: Int, fieldHeight: Int): Seq[String] = {
    val row = " " * fieldWidth
    Seq.fill(fieldHeight)(row)
  }
}
