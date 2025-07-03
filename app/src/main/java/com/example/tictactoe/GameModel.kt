package com.example.tictactoe

import kotlin.random.Random

data class GameModel(
    var gameId: String = "-1",
    var filledPos: MutableList<String> = MutableList(9) { "" },
    var winner: String = "",
    var gameStatus: GameStatus = GameStatus.CREATED,
    var currentPlayer: String = (arrayOf("X", "O"))[Random.nextInt(2)],
    var moveHistory: MutableList<Int> = mutableListOf(),
    var highlightedMove: Int? = null,
    var playerLeft: String? = null
)

enum class GameStatus {
    CREATED, JOINED, INPROGRESS, FINISHED
}
