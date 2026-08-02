package com.zhallrizqi2.chess

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val statusText = findViewById<TextView>(R.id.statusText)
        val board = findViewById<ChessBoardView>(R.id.chessBoard)
        val newGameButton = findViewById<Button>(R.id.newGameButton)

        board.onStatusChanged = { statusText.text = it }
        newGameButton.setOnClickListener { board.newGame() }
    }
}
