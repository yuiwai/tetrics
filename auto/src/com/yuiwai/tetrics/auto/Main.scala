package com.yuiwai.tetrics.auto

import com.yuiwai.tetrics.core._

object Main {
  def main(args: Array[String]): Unit = {
    import DefaultSettings._
    Evaluator.run(DefaultAutoPlayer(), 1000)
  }
}
object Evaluator extends Subscriber {
  def run(autoPlayer: AutoPlayer, loopCount: Int)(implicit setting: TetricsSetting): Unit = {
    implicit val eventBus: EventBus = EventBus()
    var stats = TetricsStats()
    subscribe { e => stats = stats(e) }
    require(loopCount > 0)
    loop(randPut(Tetrics()), autoPlayer, loopCount)
    println(stats.totalDeleted)
  }
  def loop(tetrics: Tetrics, autoPlayer: AutoPlayer, loopCount: Int)(implicit setting: TetricsSetting): Tetrics = {
    if (loopCount <= 0) tetrics
    else try {
      loop(act(tetrics, autoPlayer.act(tetrics)), autoPlayer, loopCount - 1)
    } catch {
      case _: Throwable =>
        loop(act(tetrics, autoPlayer.act(tetrics)), autoPlayer, loopCount - 2)
    }
  }
  def act(tetrics: Tetrics, action: TetricsAction)(implicit setting: TetricsSetting): Tetrics =
    action match {
      case MoveLeftAction => tetrics.moveLeft
      case MoveRightAction => tetrics.moveRight
      case MoveUpAction => tetrics.moveUp
      case MoveDownAction => tetrics.moveDown
      case DropLeftAction =>
        randPut(tetrics.dropLeft.normalizeLeft)
      case DropRightAction =>
        randPut(tetrics.dropRight.normalizeRight)
      case DropTopAction =>
        randPut(tetrics.dropTop.normalizeTop)
      case DropBottomAction =>
        randPut(tetrics.dropBottom.normalizeBottom)
      case TurnLeftAction => tetrics.turnLeft
      case TurnRightAction => tetrics.turnRight
    }
  def randPut(tetrics: Tetrics)(implicit setting: TetricsSetting): Tetrics = {
    import setting.blocks
    tetrics.putCenter(blocks((Math.random() * blocks.size).toInt))
  }
}

