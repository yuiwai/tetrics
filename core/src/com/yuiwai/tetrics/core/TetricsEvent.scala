package com.yuiwai.tetrics.core

sealed trait TetricsEvent
final case class BlockAdded(block: Block) extends TetricsEvent
final case class BlockRotated(rotationType: RotationType) extends TetricsEvent
final case class BlockMoved(moveType: MoveType) extends TetricsEvent
final case class BlockDropped(fieldType: FieldType, numRows: Int) extends TetricsEvent
final case class FieldNormalized(fieldType: FieldType, numRows: Int) extends TetricsEvent

trait EventBus {
  type Callback = TetricsEvent => Unit
  private var subscribers: Seq[Callback] = Seq.empty
  private[core] def subscribe(callback: Callback): Unit = subscribers = subscribers :+ callback
  private[core] def publish(tetricsEvent: TetricsEvent): Unit = subscribers foreach (_ (tetricsEvent))
}
object EventBus extends EventBus
trait Subscriber {
  protected def subscribe(callback: EventBus.Callback): Unit = EventBus.subscribe(callback)
}
trait Publisher {
  protected def publish(tetricsEvent: TetricsEvent): Unit = EventBus.publish(tetricsEvent)
}
