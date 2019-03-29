package com.yuiwai.tetrics.cli

import com.yuiwai.tetrics.app.{Controller, Game, Presenter}
import com.yuiwai.tetrics.core._

import scala.io.StdIn

object Main extends DefaultSettings {
  val controller = new CliController
  def main(args: Array[String]): Unit = {
    val presenter = new CliPresenter
    val game = new CliGame(presenter).start()

    loop(game)
  }
  def loop(game: Game): Unit = {
    val cmd = StdIn.readLine()
    loop(
      cmd.foldLeft(game) { (g, c) =>
        controller.inputToAction(c) match {
          case Some(a) => g.act(a)
          case _ => g
        }
      }
    )
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
    presenter.draw(Map(FieldCentral -> FieldData.fromField(tetrics.centralField)))
    tetrics
  }
}
final class CliController extends Controller[Char, Unit] {
  override def inputToAction(input: Char): Option[TetricsAction] = input match {
    case 'f' => Some(TurnRightAction)
    case 'l' => Some(MoveRightAction)
    case _ => None
  }
}
final class CliPresenter extends Presenter[FieldData] {
  private var fields: Map[FieldType, FieldData] = Seq(FieldCentral).map(_ -> FieldData.empty).toMap
  override def draw(modifiedFields: Map[FieldType, FieldData]): Unit = {
    modifiedFields.foreach(t => fields = fields.updated(t._1, t._2))
    View.render(fields)
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
final case class Pos(x: Int, y: Int)
object Pos {
  def zero = Pos(0, 0)
}
object View {
  def render(fields: Map[FieldType, FieldData]): Unit = {
    // FIXME とりあえずフィールドサイズは固定値
    val display =
      (0 to 9).foldLeft(Seq.empty[String]) { case (a, y) =>
        a :+ (0 to 9).foldLeft("") { case (b, x) =>
          b + (if (fields(FieldCentral)(x, y)) "*" else "-")
        }
      }.mkString("\n")
    println(display)
  }
}
