package com.yuiwai.tetrics.svg

import java.util.concurrent.atomic.AtomicInteger

import com.yuiwai.tetrics.core
import com.yuiwai.tetrics.core._
import japgolly.scalajs.react.Callback
import japgolly.scalajs.react.extra.Broadcaster
import org.scalajs.dom
import org.scalajs.dom.ext.KeyCode
import org.scalajs.dom.raw.KeyboardEvent

import scala.reflect.ClassTag

object App extends DefaultSettings {
  def main(args: Array[String]): Unit = init()
  def init(): Unit = {
    implicit val eventBus: core.EventBus = EventBus()
    val gameView = GameView.Component
    val controller = new GameController
    val presenter = new GamePresenter
    val game = new SingleGame(presenter)
    gameView(GameView.Props(presenter)).renderIntoDOM(dom.document.getElementById("stage"))
    dom.document.body.onkeydown = { e =>
      controller(game, e)
    }
    dom.window.setTimeout(() => game.start(), 1000)
  }
}

trait Game {
  def start(): Game
  def act(action: TetricsAction): Game
}
final class SingleGame(presenter: Presenter)(implicit setting: TetricsSetting) extends Game {
  implicit val eventBus: core.EventBus = EventBus()
  private var tetrics = Tetrics()
  override def start(): Game = {
    tetrics = draw(tetrics, randPut())
    this
  }
  override def act(action: TetricsAction): Game = {
    tetrics = draw(tetrics, tetrics.act(action))
    this
  }
  def draw(before: Tetrics, after: Tetrics): Tetrics = {
    presenter.draw(diff(before, after))
    after
  }
  def diff(before: Tetrics, after: Tetrics): Map[FieldType, FieldData] = {
    Seq(FieldLeft, FieldRight, FieldTop, FieldBottom, FieldCentral).collect {
      case fieldType if before.field(fieldType) != after.field(fieldType) =>
        fieldType -> FieldData.fromField(after.field(fieldType))
    }.toMap
  }
  def randPut()(implicit setting: TetricsSetting): Tetrics = {
    import setting.blocks
    tetrics.putCenter(blocks((Math.random() * blocks.size).toInt))
  }
}
trait Controller[I, C] {
  def apply(game: Game, input: I): Unit = inputToAction(input).foreach(game.act)
  def inputToAction(input: I): Option[TetricsAction]
}
final class GameController extends Controller[KeyboardEvent, Unit] {
  override def inputToAction(input: KeyboardEvent): Option[TetricsAction] = input.keyCode match {
    case KeyCode.F => Some(TurnRightAction)
    case KeyCode.D => Some(TurnLeftAction)
    case KeyCode.Left | KeyCode.H =>
      if (input.shiftKey) Some(DropAndNormalizeAction(DropLeftAction, NormalizeLeftAction))
      else Some(MoveLeftAction)
    case KeyCode.Right | KeyCode.L =>
      if (input.shiftKey) Some(DropAndNormalizeAction(DropRightAction, NormalizeRightAction))
      else Some(MoveRightAction)
    case KeyCode.Up | KeyCode.K =>
      if (input.shiftKey) Some(DropAndNormalizeAction(DropTopAction, NormalizeTopAction))
      else Some(MoveUpAction)
    case KeyCode.Down | KeyCode.J =>
      if (input.shiftKey) Some(DropAndNormalizeAction(DropBottomAction, NormalizeBottomAction))
      else Some(MoveDownAction)
    case _ => None
  }
}
trait Presenter {
  def draw(modifiedFields: Map[FieldType, FieldData])
}
final class GamePresenter extends Presenter with Broadcaster[ViewModel] {
  private var viewModel = ViewModel.empty.copy(
    tileSize = 15,
    fieldWidth = 10,
    fieldHeight = 10,
    centralFieldPos = Pos(150, 150)
  )
  override def draw(modifiedFields: Map[FieldType, FieldData]): Unit = {
    viewModel = viewModel.applyDiff(modifiedFields)
    broadcast(viewModel).runNow()
  }
  override def broadcast(vm: ViewModel): Callback = super.broadcast(vm)
}
final case class ViewModel(
  tileSize: Int,
  fieldWidth: Int,
  fieldHeight: Int,
  leftFieldPos: Pos,
  leftFieldData: FieldData,
  rightFieldPos: Pos,
  rightFieldData: FieldData,
  topFieldPos: Pos,
  topFieldData: FieldData,
  bottomFieldPos: Pos,
  bottomFieldData: FieldData,
  centralFieldPos: Pos,
  centralFieldData: FieldData,
) {
  def applyDiff(modifiedFields: Map[FieldType, FieldData]): ViewModel = modifiedFields.foldLeft(this) {
    case (viewModel, (fieldType, fieldData)) => fieldType match {
      case FieldLeft => viewModel.copy(leftFieldData = fieldData)
      case FieldRight => viewModel
      case FieldTop => viewModel
      case FieldBottom => viewModel
      case FieldCentral => viewModel.copy(centralFieldData = fieldData)
    }
  }
}
object ViewModel {
  def empty = ViewModel(
    0, 0, 0,
    Pos.zero, FieldData.empty,
    Pos.zero, FieldData.empty,
    Pos.zero, FieldData.empty,
    Pos.zero, FieldData.empty,
    Pos.zero, FieldData.empty
  )
}
final case class Pos(x: Int, y: Int)
object Pos {
  def zero = Pos(0, 0)
}
final case class FieldData(filled: Set[Pos]) {
  import FieldData._
  def apply(x: Int, y: Int): String = apply(Pos(x, y))
  def apply(pos: Pos): String = if (filled(pos)) RED else BLACK
}
object FieldData {
  val RED = "#ff0000"
  val BLACK = "#000000"
  def empty = FieldData(Set.empty)
  def fromField(field: Field): FieldData = FieldData {
    (for {
      y <- 0 until field.height
      x <- 0 until field.rows(y).width
      if (field.rows(y).cols & 1 << x) != 0
    } yield Pos(x, y)).toSet
  }
}

final case class EntityId[E <: Entity, I](id: I)
trait Entity
trait RootEntity[C, Ev] extends Entity {
  def applyCommand(command: C): (this.type, Ev)
}

final case class Aggregation[C, E <: RootEntity[C, Ev], Ev: ClassTag]
(private val rootEntity: E, events: Seq[Publish[Ev]] = Seq.empty) {
  type Event = Ev
  def apply(command: C): Aggregation[C, E, Ev] = {
    val (entity, event) = rootEntity.applyCommand(command)
    copy(entity, events :+ Publish(event, implicitly[ClassTag[Ev]]))
  }
}

trait Repository[E <: Entity] {
  import scala.collection.mutable
  type Id
  def nextId: EntityId[E, Id]
  def size: Int = entities.size
  private val entities: mutable.Map[EntityId[E, Id], E] = mutable.Map.empty
  def create(creator: => E): EntityId[E, Id] = {
    val newId = nextId
    entities update(newId, creator)
    newId
  }
}

trait IncrementalIdGenerator[E <: Entity] {
  self: Repository[E] =>
  type Id = Int
  private val currentId = new AtomicInteger(0)
  def nextId: EntityId[E, Id] = EntityId(currentId.incrementAndGet())
}

trait EventBus {
  import scala.collection.mutable
  import scala.reflect.ClassTag
  private val subscribes: mutable.Queue[Subscribe[_]] = mutable.Queue.empty
  def subscribe[T: ClassTag](callback: T => Unit): Subscribe[T] = {
    val subscribe = Subscribe(callback, implicitly[ClassTag[T]])
    subscribes.enqueue(subscribe)
    subscribe
  }
  def publish[T: ClassTag](event: T): Unit = subscribes.foreach { s =>
    if (s.classTag == implicitly[ClassTag[T]]) s.callback.asInstanceOf[T => Unit](event)
  }
}
final case class Subscribe[T](callback: T => Unit, classTag: ClassTag[T])
final case class Publish[T](value: T, classTag: ClassTag[T])

trait Transactional[Ev] {
  private var currentTransaction: Option[Transaction[_, Ev]] = None
  def begin[A <: Aggregation[_, _, Ev]](initialAggregation: => A): Transaction[A, Ev] = currentTransaction match {
    case Some(_) => sys.error("can't begin transaction, already exists.")
    case _ =>
      currentTransaction = Some(new Transaction[A, Ev] {
        protected val aggregation: A = initialAggregation
      })
      currentTransaction.get.asInstanceOf[Transaction[A, Ev]]
  }
  def commit(): Unit = {
    currentTransaction match {
      case Some(tx) => tx.flushEvents()
      case _ => sys.error("can't commit transaction, not exists.")
    }
    currentTransaction = None
  }
  def rollback(): Unit = ???
}
trait Transaction[A <: Aggregation[_, _, Ev], Ev] {
  private val eventBus: EventBus = new EventBus {}
  protected val aggregation: A
  def subscribe[E: ClassTag](callback: E => Unit): Subscribe[E] = eventBus.subscribe(callback)
  def modified(newAggregation: A): Transaction[A, Ev] = ???
  def flushEvents(): Unit = aggregation.events.foreach { publish =>
    eventBus.publish(publish.value)(publish.classTag)
  }
}
