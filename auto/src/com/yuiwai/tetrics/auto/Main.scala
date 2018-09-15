package com.yuiwai.tetrics.auto

import com.yuiwai.tetrics.core._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object Main {
  def main(args: Array[String]): Unit = {
    val r = Await.result(loop(100), Duration.Inf)
    println(s"Max = ${r.max}")
    println(s"Min = ${r.min}")
    println(s"Avg = ${r.sum / r.size}")
    println(s"Mean = ${r(r.size / 2)}")
  }
  def loop(loopCount: Int): Future[Seq[Int]] = {
    import DefaultSettings._
    Future.sequence((1 to loopCount).map { _ =>
      Future(Evaluator.run(DefaultAutoPlayer(), 500).totalDeleted)
    }).map(_.sorted)
  }
}
object Evaluator extends Subscriber {
  def run(autoPlayer: AutoPlayer, loopCount: Int)(implicit setting: TetricsSetting): TetricsStats = {
    implicit val eventBus: EventBus = EventBus()
    var stats = TetricsStats()
    subscribe { e => stats = stats(e) }
    require(loopCount > 0)
    loop(randPut(Tetrics()), autoPlayer, loopCount)
    stats
  }
  def loop(tetrics: Tetrics, autoPlayer: AutoPlayer, loopCount: Int)(implicit setting: TetricsSetting): Tetrics = {
    if (loopCount <= 0) tetrics
    else try {
      loop(act(tetrics, autoPlayer.act(tetrics)), autoPlayer, loopCount - 1)
    } catch {
      case _: Throwable => tetrics
    }
  }
  def act(tetrics: Tetrics, action: TetricsAction)(implicit setting: TetricsSetting): Tetrics =
    action match {
      case d: DropAction => randPut(tetrics.act(d))
      case a => tetrics.act(a)
    }
  def randPut(tetrics: Tetrics)(implicit setting: TetricsSetting): Tetrics = {
    import setting.blocks
    tetrics.putCenter(blocks((Math.random() * blocks.size).toInt))
  }
}

