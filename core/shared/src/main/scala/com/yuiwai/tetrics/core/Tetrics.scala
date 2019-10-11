package com.yuiwai.tetrics.core

case class Tetrics(
  fieldWidth: Int,
  fieldHeight: Int,
  block: Block,
  offset: Offset,
  rotation: Rotation,
  bottomField: TetricsField,
  rightField: TetricsField,
  leftField: TetricsField,
  topField: TetricsField
) {
  type R[E <: TetricsError] = Either[E, TetricsResult]
  // require(fieldWidth >= block.width + offset.x && offset.x >= 0, "block is outside of field width")
  // require(fieldHeight >= block.height + offset.y && offset.y >= 0, "block is outside of field height")
  private val emptyField = TetricsField(fieldWidth, fieldHeight)
  def field(fieldType: FieldType): TetricsField = fieldType match {
    case FieldLeft => leftField
    case FieldRight => rightField
    case FieldTop => topField
    case FieldBottom => bottomField
    case FieldCentral => centralField
  }
  def fields: Seq[TetricsField] = Seq(leftField, rightField, topField, bottomField)
  def deactivatedFields: Seq[TetricsField] = fields.filterNot(_.active)
  def centralField: TetricsField = emptyField.put(block, offset.x, offset.y)
  def put(block: Block, offset: Offset = Offset(), rotation: Rotation = Rotation0): TetricsResult =
    TetricsResult(copy(block = block, offset = offset, rotation = rotation), BlockAdded(block))
  def putCenter(block: Block): TetricsResult = put(block, Offset(
    Math.round((fieldWidth - block.width) / 2.0).toInt,
    Math.round((fieldHeight - block.height) / 2.0).toInt
  ))
  def moveLeft: R[MoveError] = offset.moveLeft match {
    case o if o.x < 0 => Left(OutOfField(o.x, 0))
    case o => Right(TetricsResult(copy(offset = o), BlockMoved(MoveLeft)))
  }
  def moveRight: R[MoveError] = offset.moveRight match {
    case o if o.x + block.width > fieldWidth => Left(OutOfField(o.x + block.width - fieldWidth, 0))
    case o => Right(TetricsResult(copy(offset = o), BlockMoved(MoveRight)))
  }
  def moveUp: R[MoveError] = offset.moveUp match {
    case o if o.y < 0 => Left(OutOfField(0, o.y))
    case o => Right(TetricsResult(copy(offset = o), BlockMoved(MoveUp)))
  }
  def moveDown: R[MoveError] = offset.moveDown match {
    case o if o.y + block.height > fieldHeight => Left(OutOfField(0, o.y + block.height - fieldHeight))
    case o => Right(TetricsResult(copy(offset = o), BlockMoved(MoveDown)))
  }
  private def turn(b: Block, r: Rotation): Either[RotateError, Tetrics] = turnedOffset(b, r) match {
    case o@Offset(x, y) =>
      val o1 = if (x < 0) OutOfField(x, 0)
      else if (x + b.width > fieldWidth) OutOfField(x + b.width - fieldWidth, 0)
      else OutOfField(0, 0)
      val o2 = if (y < 0) o1.copy(overY = y)
      else if (y + b.height > fieldHeight) o1.copy(overY = y + b.height - fieldHeight)
      else OutOfField(0, 0)
      if (o2 == OutOfField(0, 0)) Right(copy(block = b, offset = o, rotation = r))
      else Left(o2)
  }
  private def turnedOffset(b: Block, r: Rotation): Offset = Offset(
    offset.x + r.round((block.width - b.width) / 2.0),
    offset.y + r.round((block.height - b.height) / 2.0)
  )
  // TODO turnのシグネチャ更新に合わせる
  def turnLeft: R[RotateError] = turn(block.turnLeft, rotation.left) match {
    case Right(t) => Right(TetricsResult(t, BlockRotated(RotationLeft)))
    case Left(l) => Left(l)
  }
  // TODO turnのシグネチャ更新に合わせる
  def turnRight: R[RotateError] = turn(block.turnRight, rotation.right) match {
    case Right(t) => Right(TetricsResult(t, BlockRotated(RotationRight)))
    case Left(l) => Left(l)
  }
  def dropLeft: R[TetricsError] = Right(TetricsResult(copy(leftField = leftField.drop(block.turnLeft, offset.y))) {
    t => BlockDropped(FieldLeft, t.leftField.numRows, t.leftField.filledRows)
  })
  def dropRight: R[TetricsError] = Right(TetricsResult(
    copy(rightField = rightField.drop(block.turnRight, fieldHeight - offset.y - block.height))) {
    t => BlockDropped(FieldRight, t.rightField.numRows, t.rightField.filledRows)
  })
  def dropTop: R[TetricsError] = Right(TetricsResult(
    copy(topField = topField.drop(block.turnLeft.turnLeft, fieldWidth - offset.x - block.width))) {
    t => BlockDropped(FieldTop, t.topField.numRows, t.topField.filledRows)
  })
  def dropBottom: R[TetricsError] = Right(TetricsResult(
    copy(bottomField = bottomField.drop(block, offset.x))) {
    t => BlockDropped(FieldBottom, t.bottomField.numRows, t.bottomField.filledRows)
  })
  def normalizeLeft: R[TetricsError] = Right(TetricsResult(copy(leftField = leftField.normalized)) {
    t => FieldNormalized(FieldLeft, t.leftField.numRows)
  })
  def normalizeRight: R[TetricsError] = Right(TetricsResult(copy(rightField = rightField.normalized)) {
    t => FieldNormalized(FieldRight, t.rightField.numRows)
  })
  def normalizeTop: R[TetricsError] = Right(TetricsResult(copy(topField = topField.normalized)) {
    t => FieldNormalized(FieldTop, t.topField.numRows)
  })
  def normalizeBottom: R[TetricsError] = Right(TetricsResult(copy(bottomField = bottomField.normalized)) {
    t => FieldNormalized(FieldBottom, t.bottomField.numRows)
  })
  def act(action: TetricsAction): Either[TetricsError, TetricsResult] = action match {
    case MoveLeftAction => moveLeft
    case MoveRightAction => moveRight
    case MoveUpAction => moveUp
    case MoveDownAction => moveDown
    case DropLeftAction => dropLeft
    case DropRightAction => dropRight
    case DropTopAction => dropTop
    case DropBottomAction => dropBottom
    case NormalizeLeftAction => normalizeLeft
    case NormalizeRightAction => normalizeRight
    case NormalizeTopAction => normalizeTop
    case NormalizeBottomAction => normalizeBottom
    case TurnLeftAction => turnLeft
    case TurnRightAction => turnRight
    case DropAndNormalizeAction(d, n) => act(d).flatMap(_.compose(_.act(n)))
    case NoAction => Right(TetricsResult(this, NoEvent))
  }
}
object Tetrics {
  def apply(fieldWidth: Int, fieldHeight: Int): Tetrics = Tetrics(
    fieldWidth,
    fieldHeight,
    Block.empty,
    Offset(),
    Rotation0,
    TetricsField(fieldWidth, fieldHeight),
    TetricsField(fieldWidth, fieldHeight),
    TetricsField(fieldWidth, fieldHeight),
    TetricsField(fieldWidth, fieldHeight)
  )
  def apply(fieldSize: Int = 10): Tetrics = Tetrics(fieldSize, fieldSize)
}

final case class TetricsResult(tetrics: Tetrics, event: TetricsEvent) {
  def compose(f: Tetrics => TetricsResult): TetricsResult = f(tetrics) match {
    case TetricsResult(t, e) => TetricsResult(t, CompositeEvent(event, e))
  }
  def compose(f: Tetrics => Either[TetricsError, TetricsResult]): Either[TetricsError, TetricsResult] = f(tetrics)
  def tap(f: TetricsResult => Unit): TetricsResult = {
    f(this)
    this
  }
}
object TetricsResult {
  def apply(tetrics: Tetrics)(f: Tetrics => TetricsEvent): TetricsResult = apply(tetrics, f(tetrics))
}

case class Offset(x: Int = 0, y: Int = 0) {
  def +(other: Offset): Offset = Offset(x + other.x, y + other.y)
  def moveRight: Offset = copy(x = x + 1)
  def moveLeft: Offset = copy(x = x - 1)
  def moveUp: Offset = copy(y = y - 1)
  def moveDown: Offset = copy(y = y + 1)
}
object Offset {
  val zero: Offset = Offset()
}
sealed trait MoveType
case object MoveLeft extends MoveType
case object MoveRight extends MoveType
case object MoveUp extends MoveType
case object MoveDown extends MoveType
sealed trait RotationType
case object RotationLeft extends RotationType
case object RotationRight extends RotationType
sealed trait Rotation {
  def right: Rotation
  def left: Rotation
  def round(d: Double): Int
}
case object Rotation0 extends Rotation {
  lazy val right: Rotation = Rotation1
  lazy val left: Rotation = Rotation3
  def round(d: Double): Int = Math.ceil(d).toInt
}
case object Rotation1 extends Rotation {
  lazy val right: Rotation = Rotation2
  lazy val left: Rotation = Rotation0
  def round(d: Double): Int = Math.ceil(d).toInt
}
case object Rotation2 extends Rotation {
  lazy val right: Rotation = Rotation3
  lazy val left: Rotation = Rotation1
  def round(d: Double): Int = Math.floor(d).toInt
}
case object Rotation3 extends Rotation {
  lazy val right: Rotation = Rotation0
  lazy val left: Rotation = Rotation2
  def round(d: Double): Int = Math.floor(d).toInt
}
sealed trait FieldType
sealed trait DroppableField extends FieldType {
  val dropAction: DropAction
  val normalizeAction: NormalizeAction
}
case object FieldLeft extends FieldType with DroppableField {
  override val dropAction: DropAction = DropLeftAction
  override val normalizeAction: NormalizeAction = NormalizeLeftAction
}
case object FieldRight extends FieldType with DroppableField {
  override val dropAction: DropAction = DropRightAction
  override val normalizeAction: NormalizeAction = NormalizeRightAction
}
case object FieldTop extends FieldType with DroppableField {
  override val dropAction: DropAction = DropTopAction
  override val normalizeAction: NormalizeAction = NormalizeTopAction
}
case object FieldBottom extends FieldType with DroppableField {
  override val dropAction: DropAction = DropBottomAction
  override val normalizeAction: NormalizeAction = NormalizeBottomAction
}
case object FieldCentral extends FieldType
object FieldTypes {
  val all: Seq[FieldType] = Seq(FieldLeft, FieldRight, FieldTop, FieldBottom, FieldCentral)
}
sealed trait FieldStatus
case object FieldStatusActive extends FieldStatus
case object FieldStatusFrozen extends FieldStatus
case class Slice(rows: List[Row]) {
  def spaces: Int = rows.filter(_.nonEmpty) match {
    case _ :: t => t.map(_.spaces).sum
    case _ => 0
  }
  def dropPos(block: Block, y: Int = 0): Int = if (hitTest(block, y + 1)) y else dropPos(block, y + 1)
  def hitTest(block: Block, y: Int): Boolean = block.rows.zipWithIndex.exists {
    case (row, i) => if (rows.length <= y + i) true else row.hitTest(rows(y + i))
  }
}
case class Block(rows: List[Row], width: Int) {
  import RowsOps._
  require(rows.forall(_.width == width), "Block contains different width rows.")
  lazy val height: Int = rows.length
  def surface: Surface = Surface {
    turnRightRows(rows, width).map(row => (0 until height).find(i => (row.cols >> i & 1) == 1).get)
  }.normalize
  def turnRight: Block = Block(turnRightRows(rows, width), height)
  def turnLeft: Block = Block(turnLeftRows(rows, width), height)
}
object Block {
  def empty: Block = Block(Nil, 0)
  def apply(rows: String, width: Int): Block = Block(rows.grouped(width).map(Row.apply).toList, width)
}
object RowsOps {
  def turnRightRows(rows: List[Row], width: Int): List[Row] = (0 until width).map { i =>
    Row(rows.reverse.zipWithIndex.foldLeft(0) {
      case (acc, (row, j)) => acc | ((row.cols >> i & 1) << j)
    }, rows.length)
  }.toList
  def turnLeftRows(rows: List[Row], width: Int): List[Row] = (0 until width).map { i =>
    Row(rows.zipWithIndex.foldLeft(0) {
      case (acc, (row, j)) => acc | ((row.cols >> i & 1) << j)
    }, rows.length)
  }.toList.reverse
  def fillLeftRows(rows: List[Row], width: Int, height: Int): List[Row] =
    List.fill(height - rows.length)(Row(0, width)) ++ rows
}
case class Row(cols: Int, width: Int) {
  def +(that: Row, offset: Int): Row = {
    require(offset + that.width <= width, "over row width")
    copy(cols | (that.cols << offset))
  }
  def isFilled: Boolean = cols == (1 << width) - 1
  def isEmpty: Boolean = cols == 0
  def nonEmpty: Boolean = !isEmpty
  def hitTest(that: Row): Boolean = {
    require(width == that.width, "hitTest requires same width row.")
    (cols & that.cols) != 0
  }
  def asString: String = {
    (0 until width).foldLeft("") { (acc, i) =>
      acc + (cols >> i & 1)
    }
  }
  def toSeq: Seq[Boolean] = {
    (0 until width).foldLeft(Seq.empty[Boolean]) { (acc, i) =>
      acc :+ ((cols >> i & 1) == 1)
    }
  }
  def slice(x: Int, width: Int): Row = {
    val mask = (0 until width).foldLeft(0) { (acc, i) =>
      acc | (1 << i)
    }
    Row((cols >> x) & mask, width)
  }
  def count: Int = {
    (0 until width).foldLeft(0) { (acc, i) =>
      acc + (cols >> i & 1)
    }
  }
  def spaces: Int = width - count
}
object Row {
  def apply(cols: String): Row = Row(cols.zipWithIndex.foldLeft(0) {
    case (acc, ('0', _)) => acc
    case (acc, (_, i)) => acc | (1 << i)
  }, cols.length)
}

case class Surface(value: List[Int]) extends AnyVal {
  def apply(i: Int): Int = value(i)
  def size: Int = value.size
  def lift(i: Int): Surface = copy(value.map(_ + i))
  def liftTo(i: Int): Surface = value match {
    case Nil => this
    case h :: _ => lift(i - h)
  }
  def slice(offset: Int): Surface = copy(value.drop(offset))
  def slice(offset: Int, limit: Int): Surface = copy(value.slice(offset, offset + limit))
  def fitting(that: Surface): Int = value.zip(that.value) match {
    case Nil => Int.MinValue
    case l => l.foldLeft(0) {
      case (acc, (base, put)) =>
        if (base > put) fitting(that.lift(base - put))
        else acc + base - put
    }
  }
  def normalize: Surface = value match {
    case Nil => this
    case 0 :: _ => this
    case _ => liftTo(0)
  }
}

sealed trait TetricsError
sealed trait MoveError extends TetricsError
sealed trait RotateError extends TetricsError
final case class OutOfField(overX: Int, overY: Int) extends MoveError with RotateError
