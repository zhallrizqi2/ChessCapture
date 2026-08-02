package com.zhallrizqi2.chess.model

/**
 * Engine catur ringan, murni Kotlin, tanpa dependency eksternal.
 * Mendukung: gerak legal semua bidak, giliran, deteksi skak,
 * cegah gerakan yang membuat raja sendiri skak, promosi otomatis jadi ratu.
 * Tidak mendukung (disederhanakan): castling, en passant, deteksi remis 3x ulang.
 */
class ChessEngine {

    val board: Array<Array<Piece?>> = Array(8) { arrayOfNulls(8) }
    var whiteToMove = true
        private set
    var isCheck = false
        private set
    var isCheckmate = false
        private set

    init { resetBoard() }

    fun resetBoard() {
        for (r in 0..7) for (c in 0..7) board[r][c] = null
        val backRank = listOf(
            PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN,
            PieceType.KING, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK
        )
        for (c in 0..7) {
            board[0][c] = Piece(backRank[c], isWhite = false)
            board[1][c] = Piece(PieceType.PAWN, isWhite = false)
            board[6][c] = Piece(PieceType.PAWN, isWhite = true)
            board[7][c] = Piece(backRank[c], isWhite = true)
        }
        whiteToMove = true
        isCheck = false
        isCheckmate = false
    }

    fun pieceAt(sq: Square): Piece? = if (sq.isValid()) board[sq.row][sq.col] else null

    /** Semua langkah legal (sudah difilter agar tidak membuat raja sendiri skak) untuk bidak di [from]. */
    fun legalMovesFrom(from: Square): List<Square> {
        val piece = pieceAt(from) ?: return emptyList()
        if (piece.isWhite != whiteToMove) return emptyList()
        return pseudoMoves(from, piece).filter { to -> !leavesKingInCheck(from, to, piece.isWhite) }
    }

    /** Coba jalankan langkah. Return true jika berhasil (legal). */
    fun makeMove(from: Square, to: Square): Boolean {
        val piece = pieceAt(from) ?: return false
        if (piece.isWhite != whiteToMove) return false
        if (to !in legalMovesFrom(from)) return false

        board[to.row][to.col] = piece
        board[from.row][from.col] = null

        // Promosi otomatis jadi ratu
        if (piece.type == PieceType.PAWN && (to.row == 0 || to.row == 7)) {
            board[to.row][to.col] = Piece(PieceType.QUEEN, piece.isWhite)
        }

        whiteToMove = !whiteToMove
        updateCheckStatus()
        return true
    }

    private fun updateCheckStatus() {
        isCheck = isKingInCheck(whiteToMove)
        isCheckmate = isCheck && !hasAnyLegalMove(whiteToMove)
    }

    private fun hasAnyLegalMove(isWhite: Boolean): Boolean {
        for (r in 0..7) for (c in 0..7) {
            val p = board[r][c] ?: continue
            if (p.isWhite != isWhite) continue
            if (legalMovesFrom(Square(r, c)).isNotEmpty()) return true
        }
        return false
    }

    private fun leavesKingInCheck(from: Square, to: Square, isWhite: Boolean): Boolean {
        val captured = board[to.row][to.col]
        val moving = board[from.row][from.col]
        board[to.row][to.col] = moving
        board[from.row][from.col] = null

        val result = isKingInCheck(isWhite)

        board[from.row][from.col] = moving
        board[to.row][to.col] = captured
        return result
    }

    private fun isKingInCheck(isWhite: Boolean): Boolean {
        var kingSq: Square? = null
        outer@ for (r in 0..7) for (c in 0..7) {
            val p = board[r][c]
            if (p != null && p.type == PieceType.KING && p.isWhite == isWhite) {
                kingSq = Square(r, c); break@outer
            }
        }
        val king = kingSq ?: return false
        for (r in 0..7) for (c in 0..7) {
            val p = board[r][c] ?: continue
            if (p.isWhite == isWhite) continue
            if (king in pseudoMoves(Square(r, c), p, ignoreKingSafety = true)) return true
        }
        return false
    }

    /** Langkah pseudo-legal (belum dicek apakah membuat raja sendiri skak). */
    private fun pseudoMoves(from: Square, piece: Piece, ignoreKingSafety: Boolean = false): List<Square> {
        val moves = mutableListOf<Square>()
        val dirsStraight = listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)
        val dirsDiagonal = listOf(-1 to -1, -1 to 1, 1 to -1, 1 to 1)

        fun addSliding(dirs: List<Pair<Int, Int>>) {
            for ((dr, dc) in dirs) {
                var r = from.row + dr; var c = from.col + dc
                while (Square(r, c).isValid()) {
                    val target = board[r][c]
                    if (target == null) {
                        moves.add(Square(r, c))
                    } else {
                        if (target.isWhite != piece.isWhite) moves.add(Square(r, c))
                        break
                    }
                    r += dr; c += dc
                }
            }
        }

        when (piece.type) {
            PieceType.PAWN -> {
                val dir = if (piece.isWhite) -1 else 1
                val startRow = if (piece.isWhite) 6 else 1
                val oneStep = Square(from.row + dir, from.col)
                if (oneStep.isValid() && board[oneStep.row][oneStep.col] == null) {
                    moves.add(oneStep)
                    val twoStep = Square(from.row + 2 * dir, from.col)
                    if (from.row == startRow && board[twoStep.row][twoStep.col] == null) {
                        moves.add(twoStep)
                    }
                }
                for (dc in listOf(-1, 1)) {
                    val diag = Square(from.row + dir, from.col + dc)
                    if (diag.isValid()) {
                        val target = board[diag.row][diag.col]
                        if (target != null && target.isWhite != piece.isWhite) moves.add(diag)
                    }
                }
            }
            PieceType.KNIGHT -> {
                val offsets = listOf(
                    -2 to -1, -2 to 1, -1 to -2, -1 to 2,
                    1 to -2, 1 to 2, 2 to -1, 2 to 1
                )
                for ((dr, dc) in offsets) {
                    val sq = Square(from.row + dr, from.col + dc)
                    if (sq.isValid()) {
                        val target = board[sq.row][sq.col]
                        if (target == null || target.isWhite != piece.isWhite) moves.add(sq)
                    }
                }
            }
            PieceType.BISHOP -> addSliding(dirsDiagonal)
            PieceType.ROOK -> addSliding(dirsStraight)
            PieceType.QUEEN -> addSliding(dirsStraight + dirsDiagonal)
            PieceType.KING -> {
                for (dr in -1..1) for (dc in -1..1) {
                    if (dr == 0 && dc == 0) continue
                    val sq = Square(from.row + dr, from.col + dc)
                    if (sq.isValid()) {
                        val target = board[sq.row][sq.col]
                        if (target == null || target.isWhite != piece.isWhite) moves.add(sq)
                    }
                }
            }
        }
        return moves
    }
}
