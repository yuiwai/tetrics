package com.yuiwai.tetrics.core.converter

import com.yuiwai.tetrics.core._
import tetrics.{
  Request => PRequest, Response => PResponse, Tetrics => PTetrics, Action => PAction, Field => PField, Row => PRow
}
import PAction._

// FIXME 暫定でここで書く
object ProtobufConverter {
  def fromProto(pAction: PAction): TetricsAction = pAction match {
    case UNKNOWN => NoAction
    case MOVE_LEFT => MoveLeftAction
    case MOVE_RIGHT => MoveRightAction
    case MOVE_UP => MoveUpAction
    case MOVE_DOWN => MoveDownAction
    case ROTATE_LEFT => TurnLeftAction
    case ROTATE_RIGHT => TurnRightAction
    case DROP_LEFT => DropLeftAction.withNormalize
    case DROP_RIGHT => DropRightAction.withNormalize
    case DROP_TOP => DropTopAction.withNormalize
    case DROP_BOTTOM => DropBottomAction.withNormalize
  }
  def fromProto(pRequest: PRequest): Tetrics = pRequest.tetrics.map { pTetrics =>
    val (block, offset) = fromProto(pTetrics.centerField.get).trim
    Tetrics(
      10, // FIXME 定数化
      10, // FIXME 定数化
      block,
      offset,
      Rotation0,
      fromProto(pTetrics.bottomField.get),
      fromProto(pTetrics.rightField.get),
      fromProto(pTetrics.leftField.get),
      fromProto(pTetrics.topField.get)
    )
  }.get
  def fromProto(pResponse: PResponse): Seq[TetricsAction] = pResponse.action.map(fromProto)
  // FIXME 定数化
  def fromProto(pField: PField): Field = Field(pField.row.map(fromProto).toList, 10)
  // FIXME 定数化
  def fromProto(pRow: PRow): Row = Row(
    pRow.col.zipWithIndex.foldLeft(0) { case (cols, (b, i)) => cols | (if (b) 1 << i else 0) }, 10)
  def toProto(tetrics: Tetrics): PTetrics = PTetrics(
    Some(toProto(tetrics.leftField)),
    Some(toProto(tetrics.rightField)),
    Some(toProto(tetrics.topField)),
    Some(toProto(tetrics.bottomField)),
    Some(toProto(tetrics.centralField))
  )
  def toProto(field: Field): PField = PField(field.rows.map(toProto))
  def toProto(row: Row): PRow = PRow(row.toSeq)
  def toProto(actions: Seq[TetricsAction]): PResponse = PResponse(actions.map(toProto))
  def toProto(action: TetricsAction): PAction = action match {
    case NoAction => UNKNOWN
    case MoveLeftAction => MOVE_LEFT
    case MoveRightAction => MOVE_RIGHT
    case MoveUpAction => MOVE_UP
    case MoveDownAction => MOVE_DOWN
    case TurnLeftAction => ROTATE_LEFT
    case TurnRightAction => ROTATE_RIGHT
    case DropLeftAction => DROP_LEFT
    case DropRightAction => DROP_RIGHT
    case DropTopAction => DROP_TOP
    case DropBottomAction => DROP_BOTTOM
    case DropAndNormalizeAction(dropAction, _) => toProto(dropAction)
  }
}
