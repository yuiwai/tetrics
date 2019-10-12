package com.yuiwai.tetrics.core

import java.util.concurrent.LinkedBlockingQueue

sealed trait GameStatus
case object GameStatusReady extends GameStatus
case object GameStatusPlaying extends GameStatus
final case class GameStatusBlocking(
  previousStatus: GameStatus, blockedAction: Option[TetricsAction]) extends GameStatus
case object GameStatusFinished extends GameStatus
case object GameStatusAutoPlay extends GameStatus

sealed trait TetricsRule
case class TetricsSetting(fieldWidth: Int, fieldHeight: Int, blocks: Seq[Block])
trait DefaultSettings {
  implicit val setting: TetricsSetting = TetricsSetting(
    10, 10,
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
      // TODO 暫定的にEitherを強制で剥がす応急処置
      evalField(tetrics.turnLeft.right.get.tetrics, Eval(TurnLeftAction)) >>
      evalField(tetrics.turnRight.right.get.tetrics, Eval(TurnRightAction)) >>
      evalField(tetrics.turnLeft.right.get.tetrics.turnLeft.right.get.tetrics, Eval(TurnLeftAction :: TurnLeftAction :: Nil))
  }
  def evalField(tetrics: Tetrics, eval: Eval)(implicit droppableField: DroppableField): Eval = {
    evalDrop(tetrics, eval) >> evalMove(tetrics, eval)
  }
  def evalMove(tetrics: Tetrics, eval: Eval)(implicit droppableField: DroppableField): Eval = droppableField match {
    case FieldTop | FieldBottom => evalMove(tetrics, MoveRightAction, eval) >> evalMove(tetrics, MoveLeftAction, eval)
    case _ => evalMove(tetrics, MoveUpAction, eval) >> evalMove(tetrics, MoveDownAction, eval)
  }
  def evalMove(tetrics: Tetrics, moveAction: MoveAction, eval: Eval)(implicit droppableField: DroppableField): Eval =
    tetrics.act(moveAction) match {
      case Right(newTetrics) =>
        evalDrop(newTetrics.tetrics, eval.moved(moveAction)) >>
          evalMove(newTetrics.tetrics, moveAction, eval.moved(moveAction))
      case _ => Eval.failed
    }
  def evalDrop(tetrics: Tetrics, eval: Eval)(implicit droppableField: DroppableField): Eval =
    try {
      eval.dropped(evalAction(
        tetrics,
        tetrics.act(DropAndNormalizeAction(droppableField.dropAction, droppableField.normalizeAction)).right.get.tetrics
      ))
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
