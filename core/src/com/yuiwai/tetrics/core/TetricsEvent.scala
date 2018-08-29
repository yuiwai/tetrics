package com.yuiwai.tetrics.core

import com.yuiwai.tetrics.core.EventBus.Callback

sealed trait TetricsEvent
final case class GameStarted(gameType: GameType) extends TetricsEvent
final case class GameEnded(gameType: GameType) extends TetricsEvent
final case class BlockAdded(block: Block) extends TetricsEvent
final case class BlockRotated(rotationType: RotationType) extends TetricsEvent
final case class BlockMoved(moveType: MoveType) extends TetricsEvent
final case class BlockDropped(fieldType: FieldType, numRows: Int) extends TetricsEvent
final case class FieldNormalized(fieldType: FieldType, numRows: Int) extends TetricsEvent

// TODO イベントシリアライザ
trait EventSerializer {
  def serialize(event: TetricsEvent): String
  def deserialize(data: String): TetricsEvent
}

trait EventBus {
  private var subscribers: Seq[Callback] = Seq.empty
  private[core] def subscribe(callback: Callback): Unit = subscribers = subscribers :+ callback
  private[core] def publish(tetricsEvent: TetricsEvent): Unit = subscribers foreach (_ (tetricsEvent))
}
object EventBus {
  type Callback = TetricsEvent => Unit
  def apply(): EventBus = new EventBus {}
}
trait Subscriber {
  protected def subscribe(callback: EventBus.Callback)
    (implicit eventBus: EventBus): Unit = eventBus.subscribe(callback)
}
trait Publisher {
  protected def publish(tetricsEvent: TetricsEvent)
    (implicit eventBus: EventBus): Unit = eventBus.publish(tetricsEvent)
}
