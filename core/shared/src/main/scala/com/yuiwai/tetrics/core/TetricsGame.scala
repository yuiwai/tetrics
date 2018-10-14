package com.yuiwai.tetrics.core

import java.util.concurrent.LinkedBlockingQueue

import scala.util.{Success, Try}

trait TetricsGame[E, C]
  extends AnyRef
    with TetricsController[E, C]
    with TetricsView[C]
    with Publisher
    with Subscriber {
  val gameType: GameType
  private var status: GameStatus = GameStatusReady
  protected var tetrics: Tetrics
  protected val handler: TetricsEvent => Unit
  implicit val eventBus: EventBus
  protected def beforeAction(action: TetricsAction): TetricsAction = action
  protected def afterAction(action: TetricsAction): Unit = ()
  protected def modify(f: Tetrics => Tetrics): Tetrics = {
    tetrics = f(tetrics)
    tetrics
  }
  protected def block(nextAction: TetricsAction): Unit = status = GameStatusBlocking(status, Some(nextAction))
  protected def block(): Unit = status = GameStatusBlocking(status, None)
  protected def unblock()(implicit ctx: C, setting: TetricsSetting): Unit = status match {
    case GameStatusBlocking(s, a) =>
      status = s
      a.foreach(act)
    case _ => ()
  }
  def update(delta: Double)(implicit ctx: C, setting: TetricsSetting): Unit = drawAll(tetrics)
  def start()(implicit ctx: C, setting: TetricsSetting): Unit = {
    status = GameStatusPlaying
    publish(GameStarted(gameType))
    subscribe(handler)
    drawAll(randPut())
  }
  def end(): Unit = {
    status = GameStatusFinished
    unsubscribe()
    publish(GameEnded(gameType))
  }
  def readOnly()(implicit ctx: C, setting: TetricsSetting): Unit = {
    status = GameStatusPlaying
    drawAll(tetrics)
  }
  def autoPlay()(implicit ctx: C, setting: TetricsSetting): Unit = {
    status = GameStatusAutoPlay
    drawAll(randPut())
  }
  def randPut()(implicit setting: TetricsSetting): Tetrics = {
    import setting.blocks
    modify(_.putCenter(blocks((Math.random() * blocks.size).toInt)))
  }
  def input(event: E)(implicit ctx: C, setting: TetricsSetting): Unit =
    status match {
      case GameStatusPlaying => eventToAction(event) foreach act
      case _ => ()
    }
  def act(event: TetricsEvent)(implicit ctx: C, setting: TetricsSetting): Unit = event match {
    case BlockAdded(block: Block) => modify(_.putCenter(block))
    case BlockRotated(rotationType: RotationType) => act(rotationType match {
      case RotationLeft => TurnLeftAction
      case RotationRight => TurnRightAction
    })
    case BlockMoved(moveType) => act(moveType match {
      case MoveLeft => MoveLeftAction
      case MoveRight => MoveRightAction
      case MoveUp => MoveUpAction
      case MoveDown => MoveDownAction
    })
    case BlockDropped(fieldType, _, _) => act(fieldType match {
      case FieldLeft => DropAndNormalizeAction(DropLeftAction, NormalizeLeftAction)
      case FieldRight => DropAndNormalizeAction(DropRightAction, NormalizeRightAction)
      case FieldTop => DropAndNormalizeAction(DropTopAction, NormalizeTopAction)
      case FieldBottom => DropAndNormalizeAction(DropBottomAction, NormalizeBottomAction)
    })
    case _ => ()
  }
  def act(action: TetricsAction)(implicit ctx: C, setting: TetricsSetting): Unit = {
    beforeAction(action) match {
      case ma: MoveAction => modify(_.act(ma))
      case ra: RotateAction => modify(_.act(ra))
      case da: DropAndNormalizeAction =>
        modify(_.act(da))
        randPut()
      case dr: DropAction =>
        modify(_.act(dr))
        randPut()
      case NormalizeLeftAction =>
        modify(_.act(action))
      case NormalizeRightAction =>
        modify(_.act(action))
      case NormalizeTopAction =>
        modify(_.act(action))
      case NormalizeBottomAction =>
        modify(_.act(action))
      case _ =>
    }
    afterAction(action)
  }
  def act(autoPlayer: AutoPlayer)
    (implicit ctx: C, setting: TetricsSetting): Unit = if (status == GameStatusAutoPlay) try {
    act(autoPlayer.act(tetrics))
  } catch {
    case _: Throwable => ()
  }
}
sealed trait GameStatus
case object GameStatusReady extends GameStatus
case object GameStatusPlaying extends GameStatus
final case class GameStatusBlocking(
  previousStatus: GameStatus, blockedAction: Option[TetricsAction]) extends GameStatus
case object GameStatusFinished extends GameStatus
case object GameStatusAutoPlay extends GameStatus

sealed trait GameType
case object GameTypeTenTen extends GameType
abstract class TenTen[E, C](implicit val eventBus: EventBus, ctx: C)
  extends TetricsGame[E, C]
    with LabeledFieldView[C] {
  val gameType: GameType = GameTypeTenTen
  override protected var tetrics: Tetrics = Tetrics()
  private var stats: TetricsStats = TetricsStats()
  protected val handler: TetricsEvent => Unit = { e =>
    stats = stats(e)
    judge(stats, tetrics)
    drawLabels(stats)
  }
  private def judge(s: TetricsStats, t: Tetrics): Unit = {
    import TenTen._
    if (s.achieved || t.deactivatedFields.size > 2) {
      end()
    }
  }
  private def drawLabels(s: TetricsStats)(implicit ctx: C): Unit = {
    import s._
    drawLeftLabel(Label(leftDeleted))
    drawRightLabel(Label(rightDeleted))
    drawTopLabel(Label(topDeleted))
    drawBottomLabel(Label(bottomDeleted))
  }
}
object TenTen {
  type FieldMap[A] = Map[FieldType, A]
  val goalRowCount = 10
  val goalFieldCount = 2
  implicit class StatsWrap(stats: TetricsStats) {
    import stats._
    def fields: FieldMap[FieldStats] =
      Map(FieldLeft -> leftField, FieldRight -> rightField, FieldTop -> topField, FieldBottom -> bottomField)
    def achieved: Boolean = achievedFields.size >= goalFieldCount
    def achievedFields: FieldMap[FieldStats] = fields.filter(_._2.deletedRows >= goalRowCount)
  }
}
sealed trait TetricsRule
case class TetricsSetting(blocks: Seq[Block])
trait DefaultSettings {
  implicit val setting: TetricsSetting = TetricsSetting(
    Seq(
      Block("1111", 4),
      Block("1111", 2),
      Block("010111", 3),
      Block("001111", 3),
      Block("100111", 3),
      Block("110011", 3),
      Block("011110", 3)
    )
  )
}
object DefaultSettings extends DefaultSettings

trait AutoPlayer {
  def act(tetrics: Tetrics): TetricsAction
}
trait QueueingAutoPlayer extends AutoPlayer {
  protected val queue = new LinkedBlockingQueue[TetricsAction]()
  def actImpl(tetrics: Tetrics): Seq[TetricsAction]
  def act(tetrics: Tetrics): TetricsAction = {
    if (queue.isEmpty) {
      actImpl(tetrics).foreach { action =>
        queue.put(action)
      }
    }
    queue.poll()
  }
}
trait DefaultAutoPlayer extends QueueingAutoPlayer {
  case class Eval(actions: List[TetricsAction], points: Int) {
    def total: Int = points - actions.size
    def >>(eval: Eval): Eval = if (total > eval.total) this else eval
    def moved(moveAction: MoveAction): Eval = copy(moveAction :: actions)
    def dropped(point: Int)(implicit droppableField: DroppableField): Eval = {
      copy(DropAndNormalizeAction(droppableField.dropAction, droppableField.normalizeAction) :: actions, points + point)
    }
  }
  object Eval {
    def failed: Eval = Eval(Nil, Int.MinValue)
    def empty: Eval = Eval(Nil)
    def apply(actions: List[TetricsAction]): Eval = Eval(actions, 0)
    def apply(action: TetricsAction): Eval = Eval(action :: Nil)
  }
  def actImpl(tetrics: Tetrics): Seq[TetricsAction] =
    (evalRotate(tetrics)(FieldLeft) >>
      evalRotate(tetrics)(FieldRight) >>
      evalRotate(tetrics)(FieldTop) >>
      evalRotate(tetrics)(FieldBottom))
      .actions.reverse
  def evalRotate(tetrics: Tetrics)(implicit droppableField: DroppableField): Eval = {
    evalField(tetrics, Eval.empty) >>
      evalField(tetrics.turnLeft, Eval(TurnLeftAction)) >>
      evalField(tetrics.turnRight, Eval(TurnRightAction)) >>
      evalField(tetrics.turnLeft.turnLeft, Eval(TurnLeftAction :: TurnLeftAction :: Nil))
  }
  def evalField(tetrics: Tetrics, eval: Eval)(implicit droppableField: DroppableField): Eval = {
    evalDrop(tetrics, eval) >> evalMove(tetrics, eval)
  }
  def evalMove(tetrics: Tetrics, eval: Eval)(implicit droppableField: DroppableField): Eval = droppableField match {
    case FieldTop | FieldBottom => evalMove(tetrics, MoveRightAction, eval) >> evalMove(tetrics, MoveLeftAction, eval)
    case _ => evalMove(tetrics, MoveUpAction, eval) >> evalMove(tetrics, MoveDownAction, eval)
  }
  def evalMove(tetrics: Tetrics, moveAction: MoveAction, eval: Eval)(implicit droppableField: DroppableField): Eval =
    Try(tetrics.act(moveAction)) match {
      case Success(newTetrics) =>
        evalDrop(newTetrics, eval.moved(moveAction)) >>
          evalMove(newTetrics, moveAction, eval.moved(moveAction))
      case _ => Eval.failed
    }
  def evalDrop(tetrics: Tetrics, eval: Eval)(implicit droppableField: DroppableField): Eval =
    try {
      eval.dropped(evalAction(tetrics, tetrics.act(DropAndNormalizeAction(droppableField.dropAction, droppableField.normalizeAction))))
    } catch {
      case _: Throwable => Eval.failed
    }
  def evalAction(oldTetrics: Tetrics, newTetrics: Tetrics)(implicit droppableField: DroppableField): Int = {
    val (offset, width) = droppableField match {
      case FieldTop | FieldBottom => (oldTetrics.offset.x, oldTetrics.block.width)
      case _ => (oldTetrics.offset.y, oldTetrics.block.height)
    }
    ((oldTetrics.field(droppableField).slice(offset, width).spaces - newTetrics.field(droppableField).slice(offset, width).spaces) * 20) +
      (oldTetrics.field(droppableField).numRows - newTetrics.field(droppableField).numRows * 10) +
      oldTetrics.field(droppableField).numRows * 5
  }
}
object DefaultAutoPlayer {
  def apply(): DefaultAutoPlayer = new DefaultAutoPlayer {}
}
