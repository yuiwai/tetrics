package com.yuiwai.tetrics

import java.util.concurrent.atomic.AtomicInteger

import scala.reflect.ClassTag

package object experimental {
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
}
