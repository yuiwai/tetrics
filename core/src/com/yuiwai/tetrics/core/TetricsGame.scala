package com.yuiwai.tetrics.core

trait TetricsGame[E, C]
  extends AnyRef
    with TetricsController[E, C]
    with TetricsView[C] {
  protected var tetrics: Tetrics
  implicit protected val eventBus: EventBus = EventBus()
  private def modify(f: Tetrics => Tetrics): Tetrics = {
    tetrics = f(tetrics)
    tetrics
  }
  def start()(implicit ctx: C, setting: TetricsSetting): Unit
  def randPut()(implicit setting: TetricsSetting): Tetrics = {
    import setting.blocks
    modify(_.putCenter(blocks((Math.random() * blocks.size).toInt)))
  }
  def input(event: E)(implicit ctx: C, setting: TetricsSetting): Unit = eventToAction(event) foreach {
    case MoveLeftAction => drawCentral(modify(_.moveLeft))
    case MoveRightAction => drawCentral(modify(_.moveRight))
    case MoveUpAction => drawCentral(modify(_.moveUp))
    case MoveDownAction => drawCentral(modify(_.moveDown))
    case DropLeftAction =>
      drawLeft(modify(_.dropLeft.normalizeLeft))
      drawCentral(randPut())
    case DropRightAction =>
      drawRight(modify(_.dropRight.normalizeRight))
      drawCentral(randPut())
    case DropTopAction =>
      drawTop(modify(_.dropTop.normalizeTop))
      drawCentral(randPut())
    case DropBottomAction =>
      drawBottom(modify(_.dropBottom.normalizeBottom))
      drawCentral(randPut())
    case TurnLeftAction => drawCentral(modify(_.turnLeft))
    case TurnRightAction => drawCentral(modify(_.turnRight))
  }
}
abstract class TenTen[E, C]
  extends TetricsGame[E, C]
    with LabeledFieldView[C]
    with Subscriber {
  override protected var tetrics: Tetrics = Tetrics(10)
  private var stats: TetricsStats = TetricsStats()
  override def start()(implicit ctx: C, setting: TetricsSetting): Unit = {
    subscribe { e =>
      stats = stats(e)
      judge(stats)
      drawLabels(stats)
    }
    drawAll(randPut())
  }
  private def judge(s: TetricsStats): Unit = {
    import TenTen._
    s.fields
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
  val goal = 10
  implicit class StatsWrap(stats: TetricsStats) {
    import stats._
    def fields: Seq[FieldStats] = Seq(leftField, rightField, topField, bottomField)
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

