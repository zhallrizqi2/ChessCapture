package com.zhallrizqi2.chess

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.zhallrizqi2.chess.model.ChessEngine
import com.zhallrizqi2.chess.model.Square

class ChessBoardView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val engine = ChessEngine()

    private val lightPaint = Paint().apply { color = Color.parseColor("#EEEED2") }
    private val darkPaint = Paint().apply { color = Color.parseColor("#769656") }
    private val selectedPaint = Paint().apply { color = Color.parseColor("#88F6F669") }
    private val moveHintPaint = Paint().apply {
        color = Color.parseColor("#66000000")
        style = Paint.Style.FILL
    }
    private val piecePaint = Paint().apply {
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }
    private val statusPaint = Paint().apply {
        isAntiAlias = true
        color = Color.DKGRAY
        textAlign = Paint.Align.CENTER
    }

    private var selected: Square? = null
    private var legalTargets: List<Square> = emptyList()
    var onStatusChanged: ((String) -> Unit)? = null

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cell = width / 8f
        piecePaint.textSize = cell * 0.72f

        for (r in 0..7) {
            for (c in 0..7) {
                val paint = if ((r + c) % 2 == 0) lightPaint else darkPaint
                canvas.drawRect(c * cell, r * cell, (c + 1) * cell, (r + 1) * cell, paint)
            }
        }

        selected?.let { sel ->
            canvas.drawRect(sel.col * cell, sel.row * cell, (sel.col + 1) * cell, (sel.row + 1) * cell, selectedPaint)
        }
        for (target in legalTargets) {
            canvas.drawCircle(target.col * cell + cell / 2, target.row * cell + cell / 2, cell * 0.15f, moveHintPaint)
        }

        for (r in 0..7) for (c in 0..7) {
            val piece = engine.board[r][c] ?: continue
            val cx = c * cell + cell / 2
            val cy = r * cell + cell / 2 - (piecePaint.ascent() + piecePaint.descent()) / 2
            piecePaint.color = if (piece.isWhite) Color.WHITE else Color.BLACK
            piecePaint.style = Paint.Style.FILL
            canvas.drawText(piece.symbol(), cx, cy, piecePaint)
        }

        reportStatus()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_DOWN) return true
        if (engine.isCheckmate) return true

        val cell = width / 8f
        val row = (event.y / cell).toInt().coerceIn(0, 7)
        val col = (event.x / cell).toInt().coerceIn(0, 7)
        val tapped = Square(row, col)

        val sel = selected
        if (sel != null && tapped in legalTargets) {
            engine.makeMove(sel, tapped)
            selected = null
            legalTargets = emptyList()
        } else {
            val piece = engine.pieceAt(tapped)
            if (piece != null && piece.isWhite == engine.whiteToMove) {
                selected = tapped
                legalTargets = engine.legalMovesFrom(tapped)
            } else {
                selected = null
                legalTargets = emptyList()
            }
        }
        invalidate()
        return true
    }

    private fun reportStatus() {
        val turn = if (engine.whiteToMove) "Putih" else "Hitam"
        val text = when {
            engine.isCheckmate -> "Skakmat! ${if (engine.whiteToMove) "Hitam" else "Putih"} menang"
            engine.isCheck -> "Giliran $turn — Skak!"
            else -> "Giliran $turn"
        }
        onStatusChanged?.invoke(text)
    }

    fun newGame() {
        engine.resetBoard()
        selected = null
        legalTargets = emptyList()
        invalidate()
    }
}
