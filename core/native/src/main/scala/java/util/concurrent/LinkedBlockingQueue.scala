package java.util.concurrent

import scala.collection.mutable

class LinkedBlockingQueue[T] {
  private val queue = mutable.Queue.empty[T]
  def isEmpty: Boolean = queue.isEmpty
  def put(item: T): Unit = queue.enqueue(item)
  def poll(): T = queue.dequeue()
}
