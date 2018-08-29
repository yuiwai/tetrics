package com.yuiwai.tetrics.core

trait TetricsGame[E, C]
  extends AnyRef
    with TetricsController[E, C]
    with TetricsView[C]
    with Publisher {
  val gameType: GameType
  protected var tetrics: Tetrics
  implicit val eventBus: EventBus
  private def modify(f: Tetrics => Tetrics): Tetrics = {
    tetrics = f(tetrics)
    tetrics
  }
  def start()(implicit ctx: C, setting: TetricsSetting): Unit = {
    publish(GameStarted(gameType))
  }
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
sealed trait GameType
case object GameTypeTenTen extends GameType
abstract class TenTen[E, C](implicit val eventBus: EventBus)
  extends TetricsGame[E, C]
    with LabeledFieldView[C]
    with Subscriber {
  val gameType: GameType = GameTypeTenTen
  override protected var tetrics: Tetrics = Tetrics()
  private var stats: TetricsStats = TetricsStats()
  override def start()(implicit ctx: C, setting: TetricsSetting): Unit = {
    super.start()
    subscribe { e =>
      stats = stats(e)
      judge(stats)
      drawLabels(stats)
    }
    drawAll(randPut())
  }
  private def judge(s: TetricsStats): Unit = {
    import TenTen._
    if (s.achieved) {
      publish(GameEnded(gameType))
    }
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
  type FieldMap[A] = Map[FieldType, A]
  val goalRowCount = 10
  val goalFieldCount = 2
  implicit class StatsWrap(stats: TetricsStats) {
    import stats._
    def fields: FieldMap[FieldStats] =
      Map(FieldLeft -> leftField, FieldRight -> rightField, FieldTop -> topField, FieldBottom -> bottomField)
    def achieved: Boolean = achievedFields.size >= goalFieldCount
    def achievedFields: FieldMap[FieldStats] = fields.filter(_._2.deletedRows >= goalRowCount)
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

