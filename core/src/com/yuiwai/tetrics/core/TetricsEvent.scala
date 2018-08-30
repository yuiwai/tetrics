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
  private var subscribers: Map[Subscriber, Callback] = Map.empty
  private[core] def subscribe(subscriber: Subscriber, callback: Callback): Unit = {
    subscribers = subscribers + (subscriber -> callback)
  }
  private[core] def unsubscribe(subscriber: Subscriber): Unit = subscribers = subscribers - subscriber
  private[core] def publish(tetricsEvent: TetricsEvent): Unit = subscribers.values foreach (_ (tetricsEvent))
}
object EventBus {
  type Callback = TetricsEvent => Unit
  def apply(): EventBus = new EventBus {}
}
trait Subscriber {
  protected def subscribe(callback: EventBus.Callback)
    (implicit eventBus: EventBus): Unit = eventBus.subscribe(this, callback)
  protected def unsubscribe()(implicit eventBus: EventBus): Unit = eventBus.unsubscribe(this)
}
trait Publisher {
  protected def publish(tetricsEvent: TetricsEvent)
    (implicit eventBus: EventBus): Unit = eventBus.publish(tetricsEvent)
}
