package com.yuiwai.tetrics.core

case class Tetrics(
  fieldSize: Int,
  block: Block,
  offset: Offset,
  rotation: Rotation,
  bottomField: Field,
  rightField: Field,
  leftField: Field,
  topField: Field
) {
  require(fieldSize >= block.width + offset.x && offset.x >= 0, "block is outside of field")
  require(fieldSize >= block.height + offset.y && offset.y >= 0, "block is outside of field")
  private val emptyField = Field(fieldSize)
  def centralField: Field = emptyField.put(block, offset.x, offset.y)
  def put(block: Block): Tetrics = copy(block = block)
  def putCenter(block: Block): Tetrics = copy(block = block, offset = Offset(
    Math.round((fieldSize - block.width) / 2.0).toInt,
    Math.round((fieldSize - block.height) / 2.0).toInt
  ))
  def moveRight: Tetrics = copy(offset = offset.moveRight)
  def moveLeft: Tetrics = copy(offset = offset.moveLeft)
  def moveUp: Tetrics = copy(offset = offset.moveUp)
  def moveDown: Tetrics = copy(offset = offset.moveDown)
  protected def turn(b: Block, r: Rotation): Tetrics =
    copy(block = b, offset = turnedOffset(b, r), rotation = r)
  protected def turnedOffset(b: Block, r: Rotation): Offset = Offset(
    offset.x + r.round((block.width - b.width) / 2.0),
    offset.y + r.round((block.height - b.height) / 2.0)
  )
  def turnRight: Tetrics = turn(block.turnRight, rotation.right)
  def turnLeft: Tetrics = turn(block.turnLeft, rotation.left)
  def dropBottom: Tetrics = copy(bottomField = bottomField.drop(block, offset.x))
  def dropRight: Tetrics = copy(rightField = rightField.drop(block.turnRight, fieldSize - offset.y - block.height))
  def dropLeft: Tetrics = copy(leftField = leftField.drop(block.turnLeft, offset.y))
  def dropTop: Tetrics = copy(topField = topField.drop(block.turnLeft.turnLeft, fieldSize - offset.x - block.width))
  def normalized: Tetrics = copy(
    bottomField = bottomField.normalized,
    rightField = rightField.normalized,
    leftField = leftField.normalized,
    topField = topField.normalized
  )
}
object Tetrics {
  def apply(fieldSize: Int): Tetrics = Tetrics(
    fieldSize,
    Block.empty,
    Offset(),
    Rotation0,
    Field(fieldSize),
    Field(fieldSize),
    Field(fieldSize),
    Field(fieldSize)
  )
}
case class Offset(x: Int = 0, y: Int = 0) {
  def moveRight: Offset = copy(x = x + 1)
  def moveLeft: Offset = copy(x = x - 1)
  def moveUp: Offset = copy(y = y - 1)
  def moveDown: Offset = copy(y = y + 1)
}
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
case class Field(rows: List[Row], width: Int) {
  import RowsOps._
  require(!rows.exists(_.width != width), "Field contains different width rows.")
  lazy val height: Int = rows.length
  def drop(block: Block, offset: Int): Field = put(block, offset, slice(offset, block.width).dropPos(block))
  def put(block: Block, x: Int, y: Int): Field = copy(put(rows, block.rows, x, y))
  protected def put(baseRows: List[Row], putRows: List[Row], x: Int, y: Int, resultRows: List[Row] = Nil): List[Row] =
    putRows match {
      case Nil => resultRows ++ baseRows
      case _ if y > 0 => put(baseRows.tail, putRows, x, y - 1, resultRows :+ baseRows.head)
      case h :: t => put(baseRows.tail, t, x, 0, resultRows :+ (baseRows.head + (h, x)))
    }
  def putCenter(block: Block, offset: Offset = Offset()): Field =
    put(
      block,
      Math.round((width - block.width) / 2.0).toInt + offset.x,
      Math.round((rows.length - block.height) / 2.0).toInt + offset.y
    )
  def slice(offset: Int, width: Int): Slice = Slice(rows.map(row => row.copy(row.cols >> offset, width)))
  def normalized: Field = copy(fillLeftRows(rows.filter(!_.isFilled), width, height))
  def count: Int = rows.map(c => bitCount(c.cols)).sum
  private def bitCount(i: Int, count: Int = 0): Int = if (i == 0) count else bitCount(i & i - 1, count + 1)
  def turnRight: Field = Field(turnRightRows(rows, width), height)
  def turnLeft: Field = Field(turnLeftRows(rows, width), height)
}
object Field {
  def apply(size: Int): Field = Field(size, size)
  def apply(width: Int, height: Int): Field = Field(List.fill(height)(Row(0, width)), width)
}
case class Slice(rows: List[Row]) {
  def dropPos(block: Block, y: Int = 0): Int = if (hitTest(block, y + 1)) y else dropPos(block, y + 1)
  def hitTest(block: Block, y: Int): Boolean = block.rows.zipWithIndex.exists {
    case (row, i) => if (rows.length <= y + i) true else row.hitTest(rows(y + i))
  }
}
case class Block(rows: List[Row], width: Int) {
  import RowsOps._
  require(rows.forall(_.width == width), "Block contains different width rows.")
  lazy val height: Int = rows.length
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
}
object Row {
  def apply(cols: String): Row = Row(cols.zipWithIndex.foldLeft(0) {
    case (acc, ('0', _)) => acc
    case (acc, (_, i)) => acc | (1 << i)
  }, cols.length)
}
