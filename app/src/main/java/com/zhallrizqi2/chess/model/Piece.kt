package com.zhallrizqi2.chess.model

enum class PieceType { PAWN, KNIGHT, BISHOP, ROOK, QUEEN, KING }

data class Piece(val type: PieceType, val isWhite: Boolean) {
    fun symbol(): String = when (type) {
        PieceType.PAWN -> if (isWhite) "\u2659" else "\u265F"
        PieceType.KNIGHT -> if (isWhite) "\u2658" else "\u265E"
        PieceType.BISHOP -> if (isWhite) "\u2657" else "\u265D"
        PieceType.ROOK -> if (isWhite) "\u2656" else "\u265C"
        PieceType.QUEEN -> if (isWhite) "\u2655" else "\u265B"
        PieceType.KING -> if (isWhite) "\u2654" else "\u265A"
    }
}

data class Square(val row: Int, val col: Int) {
    fun isValid() = row in 0..7 && col in 0..7
}
