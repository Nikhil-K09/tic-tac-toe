package com.example.tictactoe

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tictactoe.databinding.ActivityGameBinding
import com.google.firebase.firestore.FirebaseFirestore

class GameActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var binding: ActivityGameBinding
    private var gameModel: GameModel? = null
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        GameData.fetchGameModel()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        listOf(
            binding.btn0, binding.btn1, binding.btn2, binding.btn3, binding.btn4,
            binding.btn5, binding.btn6, binding.btn7, binding.btn8
        ).forEach { it.setOnClickListener(this) }

        binding.startGameBtn.setOnClickListener { startGame() }

        GameData.gameModel.observe(this) {
            gameModel = it
            it?.let { model ->
                if (model.playerLeft != null && model.playerLeft != GameData.myID) {
                    Toast.makeText(this, "Opponent left the game", Toast.LENGTH_LONG).show()
                    firestore.collection("games").document(model.gameId).delete()
                    finish()
                }
            }
            setUI()
        }
    }

    private fun setUI() {
        gameModel?.apply {
            val buttons = listOf(
                binding.btn0, binding.btn1, binding.btn2,
                binding.btn3, binding.btn4, binding.btn5,
                binding.btn6, binding.btn7, binding.btn8
            )

            buttons.forEachIndexed { index, btn ->
                btn.text = filledPos[index]
                btn.alpha = if (index == highlightedMove) 0.4f else 1.0f
            }

            // Set visibility of the start button based on game status
            binding.startGameBtn.visibility = when (gameStatus) {
                GameStatus.CREATED -> View.VISIBLE
                GameStatus.JOINED -> View.VISIBLE
                GameStatus.INPROGRESS -> View.INVISIBLE
                GameStatus.FINISHED -> View.VISIBLE // Optional: You can set this to INVISIBLE if you prefer
            }

            binding.gameStatusText.text = when (gameStatus) {
                GameStatus.CREATED -> "Game ID: $gameId"
                GameStatus.JOINED -> "Click Start Game"
                GameStatus.INPROGRESS -> if (GameData.myID == currentPlayer) "Your turn" else "$currentPlayer's turn"
                GameStatus.FINISHED -> if (winner.isNotEmpty()) "$winner won" else "Draw"
            }
        }
    }


    private fun startGame() {
        gameModel?.apply {
            updateGameData(copy(gameStatus = GameStatus.INPROGRESS))
        }
    }

    private fun updateGameData(model: GameModel) {
        GameData.saveGameModel(model)
    }

    private fun checkForWinner() {
        val winningPos = arrayOf(
            intArrayOf(0, 1, 2), intArrayOf(3, 4, 5), intArrayOf(6, 7, 8),
            intArrayOf(0, 3, 6), intArrayOf(1, 4, 7), intArrayOf(2, 5, 8),
            intArrayOf(0, 4, 8), intArrayOf(2, 4, 6)
        )
        gameModel?.apply {
            for (pos in winningPos) {
                if (pos.any { it == highlightedMove }) continue
                if (filledPos[pos[0]] == filledPos[pos[1]] &&
                    filledPos[pos[1]] == filledPos[pos[2]] &&
                    filledPos[pos[0]].isNotEmpty()) {
                    gameStatus = GameStatus.FINISHED
                    winner = filledPos[pos[0]]
                }
            }
            updateGameData(this)
        }
    }

    override fun onClick(v: View?) {
        gameModel?.apply {
            if (gameStatus != GameStatus.INPROGRESS) {
                Toast.makeText(this@GameActivity, "Game not started", Toast.LENGTH_SHORT).show()
                return
            }
            if (gameId != "-1" && currentPlayer != GameData.myID) {
                Toast.makeText(this@GameActivity, "Not your turn", Toast.LENGTH_SHORT).show()
                return
            }

            val clickedPos = (v?.tag as String).toInt()
            if (filledPos[clickedPos].isEmpty()) {
                filledPos[clickedPos] = currentPlayer
                moveHistory.add(clickedPos)

                highlightedMove = if (moveHistory.size > 5) moveHistory[0] else null

                if (moveHistory.size > 6) {
                    val removed = moveHistory.removeAt(0)
                    filledPos[removed] = ""
                    highlightedMove = if (moveHistory.size > 5) moveHistory[0] else null
                }

                currentPlayer = if (currentPlayer == "X") "O" else "X"
                checkForWinner()
                updateGameData(this)
            }
        }
    }

    override fun onBackPressed() {
        handlePlayerExit()
        super.onBackPressed()
    }

    override fun onDestroy() {
        handlePlayerExit()
        super.onDestroy()
    }

    private fun handlePlayerExit() {
        gameModel?.apply {
            if (gameId != "-1" && gameStatus == GameStatus.INPROGRESS) {
                playerLeft = GameData.myID
                updateGameData(this)
            } else if (gameStatus == GameStatus.FINISHED) {
                firestore.collection("games").document(gameId).delete()
            }
        }
    }
}
