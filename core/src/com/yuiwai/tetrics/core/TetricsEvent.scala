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

trait EventSerializer[T] {
  val setting: TetricsSetting
  def serialize(event: TetricsEvent): T
  def deserialize(data: T): TetricsEvent
  def gameType2Int(gameType: GameType): Int = gameType match {
    case GameTypeTenTen => 1
  }
  def rotationType2Int(rotationType: RotationType): Int = rotationType match {
    case RotationLeft => 1
    case RotationRight => 2
  }
  def moveType2Int(moveType: MoveType): Int = moveType match {
    case MoveLeft => 1
    case MoveRight => 2
    case MoveUp => 3
    case MoveDown => 4
  }
  def fieldType2Int(fieldType: FieldType): Int = fieldType match {
    case FieldLeft => 1
    case FieldRight => 2
    case FieldTop => 3
    case FieldBottom => 4
    case FieldCentral => 5
  }
  def block2Int(block: Block): Int = setting.blocks.indexOf(block)
}

// FIXME 暫定実装
trait ByteEventSerializer extends EventSerializer[Array[Byte]] {
  override def serialize(event: TetricsEvent): Array[Byte] = event match {
    case GameStarted(gameType) => s(1) ++ s(gameType)
    case GameEnded(gameType: GameType) => s(2) ++ s(gameType)
    case BlockAdded(block: Block) => s(3) ++ s(block)
    case BlockRotated(rotationType: RotationType) => s(4) ++ s(rotationType)
    case BlockMoved(moveType: MoveType) => s(5) ++ s(moveType)
    case BlockDropped(fieldType: FieldType, numRows: Int) => s(6) ++ s(fieldType) ++ s(numRows)
    case FieldNormalized(fieldType: FieldType, numRows: Int) => s(7) ++ s(fieldType) ++ s(numRows)
  }
  private def s(intVal: Int): Array[Byte] = Array(intVal.toByte)
  private def s(gameType: GameType): Array[Byte] = s(gameType2Int(gameType))
  private def s(rotationType: RotationType): Array[Byte] = s(rotationType2Int(rotationType))
  private def s(moveType: MoveType): Array[Byte] = s(moveType2Int(moveType))
  private def s(fieldType: FieldType): Array[Byte] = s(fieldType2Int(fieldType))
  private def s(block: Block): Array[Byte] = s(block2Int(block))
  override def deserialize(data: Array[Byte]): TetricsEvent = data.head.toInt match {
    case 1 => GameStarted(a2gt(data.tail))
    case 2 => GameEnded(a2gt(data.tail))
    case 3 => BlockAdded(a2bl(data.tail))
    case 4 => BlockRotated(a2rt(data.tail))
    case 5 => BlockMoved(a2mt(data.tail))
    case 6 => BlockDropped(a2ft(data.tail), data.drop(2).head.toInt)
    case 7 => FieldNormalized(a2ft(data.tail), data.drop(2).head.toInt)
  }
  private def a2gt(a: Array[Byte]): GameType = a.head.toInt match {
    case 1 => GameTypeTenTen
  }
  private def a2bl(a: Array[Byte]): Block = setting.blocks(a.head.toInt)
  private def a2rt(a: Array[Byte]): RotationType = a.head.toInt match {
    case 1 => RotationLeft
    case 2 => RotationRight
  }
  private def a2mt(a: Array[Byte]): MoveType = a.head.toInt match {
    case 1 => MoveLeft
    case 2 => MoveRight
    case 3 => MoveUp
    case 4 => MoveDown
  }
  private def a2ft(a: Array[Byte]): FieldType = a.head.toInt match {
    case 1 => FieldLeft
    case 2 => FieldRight
    case 3 => FieldTop
    case 4 => FieldBottom
    case 5 => FieldCentral
  }
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
