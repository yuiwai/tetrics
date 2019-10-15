package com.yuiwai.tetrics.core

case class TetricsField(rows: List[Row], width: Int, status: FieldStatus = FieldStatusActive) {
  import RowsOps._
  require(!rows.exists(_.width != width), "Field contains different width rows.")
  lazy val height: Int = rows.length
  lazy val numRows: Int = rows.count(_.nonEmpty)
  def surface: Surface = Surface {
    turnRightRows(rows, width).map {
      case row if row.isEmpty => 0
      case row => (0 until width).foldLeft(0)((acc, i) => if ((row.cols >> i & 1) == 1) i + 1 else acc)
    }
  }
  def hitTest(x: Int, y: Int): Boolean = (rows(y).cols & 1 << x) != 0
  def active: Boolean = status == FieldStatusActive
  def freeze: TetricsField = copy(status = FieldStatusFrozen)
  def drop(block: Block, offset: Int): Either[FieldError, TetricsField] = {
    if (!active) Left(FieldIsFrozen)
    else {
      val sliced = slice(offset, block.width)
      val dropPos = sliced.dropPos(block)
      val dropped = put(block, offset, dropPos)
      if (sliced.hitTest(block, dropPos)) Right(dropped.freeze) else Right(dropped)
    }
  }
  def put(block: Block, x: Int, y: Int): TetricsField = copy(put(rows, block.rows, x, y))
  protected def put(baseRows: List[Row], putRows: List[Row], x: Int, y: Int, resultRows: List[Row] = Nil): List[Row] =
    putRows match {
      case Nil => resultRows ++ baseRows
      case _ if y > 0 => put(baseRows.tail, putRows, x, y - 1, resultRows :+ baseRows.head)
      case h :: t => put(baseRows.tail, t, x, 0, resultRows :+ (baseRows.head + (h, x)))
    }
  def putCenter(block: Block, offset: Offset = Offset()): TetricsField =
    put(
      block,
      Math.round((width - block.width) / 2.0).toInt + offset.x,
      Math.round((rows.length - block.height) / 2.0).toInt + offset.y
    )
  def filledRows: Seq[Int] = rows.zipWithIndex.collect { case (row, i) if row.isFilled => i }
  def slice(offset: Int, width: Int): Slice = Slice(rows.map(row => row.copy(row.cols >> offset, width)))
  def normalized: TetricsField = copy(fillLeftRows(rows.filter(!_.isFilled), width, height))
  def count: Int = rows.map(c => bitCount(c.cols)).sum
  private def bitCount(i: Int, count: Int = 0): Int = if (i == 0) count else bitCount(i & i - 1, count + 1)
  def turnRight: TetricsField = copy(turnRightRows(rows, width), height)
  def turnLeft: TetricsField = copy(turnLeftRows(rows, width), height)
  def offset: Option[Offset] = for {
    x <- offsetX
    y <- offsetY
  } yield Offset(x, y)
  def offsetX: Option[Int] = turnRight.offsetY
  def offsetY: Option[Int] = rows.zipWithIndex.find(_._1.nonEmpty).map(_._2)
  def region(offset: Offset, width: Int, height: Int): Block = {
    val rs = rows.slice(offset.y, offset.y + height).map(_.slice(offset.x, width))
    Block(rs, width)
  }
  def trim: (Block, Offset) =
    (for {
      os <- offset
      oe <- turnRight.turnRight.offset
    } yield region(os, width - oe.x - os.x, height - oe.y - os.y) -> os)
      .getOrElse(Block.empty, Offset.zero)
}
object TetricsField {
  def apply(size: Int): TetricsField = TetricsField(size, size)
  def apply(width: Int, height: Int): TetricsField = TetricsField(List.fill(height)(Row(0, width)), width)
}
sealed trait FieldError
case object FieldIsFrozen extends FieldError