package com.example.tictactoe

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tictactoe.databinding.ActivityGameBinding
import com.example.tictactoe.databinding.ActivityMainBinding

class GameActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var binding: ActivityGameBinding
    private var gameModel: GameModel? =null
    private lateinit var buttonMap: Map<Int, android.widget.Button>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityGameBinding.inflate(layoutInflater)

        enableEdgeToEdge()
        setContentView(binding.root)
        GameData.fetchGameModel()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.btn0.setOnClickListener(this)
        binding.btn1.setOnClickListener(this)
        binding.btn2.setOnClickListener(this)
        binding.btn3.setOnClickListener(this)
        binding.btn4.setOnClickListener(this)
        binding.btn5.setOnClickListener(this)
        binding.btn6.setOnClickListener(this)
        binding.btn7.setOnClickListener(this)
        binding.btn8.setOnClickListener(this)

        buttonMap = mapOf(
            0 to binding.btn0,
            1 to binding.btn1,
            2 to binding.btn2,
            3 to binding.btn3,
            4 to binding.btn4,
            5 to binding.btn5,
            6 to binding.btn6,
            7 to binding.btn7,
            8 to binding.btn8
        )


        binding.startGameBtn.setOnClickListener{
            startGame()
        }

        GameData.gameModel.observe(this){
            gameModel=it
            setUI()
        }

    }
    fun setUI() {
        gameModel?.apply {
            val allButtons = listOf(
                binding.btn0, binding.btn1, binding.btn2,
                binding.btn3, binding.btn4, binding.btn5,
                binding.btn6, binding.btn7, binding.btn8
            )

            for ((index, btn) in allButtons.withIndex()) {
                btn.text = filledPos[index]

                // Highlight the oldest move
                if (index == highlightedMove) {
                    btn.alpha = 0.4f  // faded
                } else {
                    btn.alpha = 1.0f  // normal
                }
            }
            binding.startGameBtn.visibility=View.VISIBLE

            binding.gameStatusText.text =
                when(gameStatus){
                    GameStatus.CREATED -> {
                        binding.startGameBtn.visibility = View.INVISIBLE
                        "Game ID :"+ gameId
                    }
                    GameStatus.JOINED ->{
                        "Click on start game"
                    }
                    GameStatus.INPROGRESS ->{
                        binding.startGameBtn.visibility = View.INVISIBLE
                        when(GameData.myID){
                            currentPlayer -> "Your turn"
                            else ->  currentPlayer + "'s turn"
                        }

                    }
                    GameStatus.FINISHED ->{
                        if(winner.isNotEmpty()) {
                            when(GameData.myID){
                                winner -> "You won"
                                else ->   winner + " Won"
                            }

                        }
                        else "DRAW"
                    }
                }
        }
    }

    fun startGame() {
        gameModel?.apply {
            updateGameData(
                GameModel(
                    gameId =gameId,
                    gameStatus =GameStatus.INPROGRESS
                )
            )
        }
    }
    fun updateGameData(model :GameModel){
        GameData.saveGameModel(model)
    }
    fun checkForWinner(){
        val winningPos=arrayOf(
            intArrayOf(0,1,2),
            intArrayOf(3,4,5),
            intArrayOf(6,7,8),

            intArrayOf(0,3,6),
            intArrayOf(1,4,7),
            intArrayOf(2,5,8),

            intArrayOf(0,4,8),
            intArrayOf(2,4,6)
        )

        gameModel?.apply {

            for(i in winningPos){
                if (i.any { it == highlightedMove }) continue

                if(filledPos[i[0]]==filledPos[i[1]] && filledPos[i[1]]==filledPos[i[2]] && filledPos[i[0]].isNotEmpty()){
                    gameStatus=GameStatus.FINISHED
                    winner=filledPos[i[0]]
                }
            }
            if(filledPos.none(){it.isEmpty()}){
                gameStatus=GameStatus.FINISHED

            }
            updateGameData(this)
        }

    }

    override fun onClick(v: View?) {
        gameModel?.apply {
            if (gameStatus != GameStatus.INPROGRESS) {
                Toast.makeText(applicationContext, "Game Not Started", Toast.LENGTH_SHORT).show()
                return
            }
            if(gameId!="-1" && currentPlayer!=GameData.myID){
                Toast.makeText(applicationContext, "Not Your Turn", Toast.LENGTH_SHORT).show()
                return
            }

            val clickedPos = (v?.tag as String).toInt()
            if (filledPos[clickedPos].isEmpty()) {
                filledPos[clickedPos] = currentPlayer
                moveHistory.add(clickedPos)

                highlightedMove = if (moveHistory.size > 5) moveHistory[0] else null

                // Remove oldest move after 7th click
                if (moveHistory.size > 6) {
                    val removedPos = moveHistory.removeAt(0)
                    filledPos[removedPos] = ""
                    highlightedMove = if (moveHistory.size > 5) moveHistory[0] else null
                }

                currentPlayer = if (currentPlayer == "X") "O" else "X"
                checkForWinner()
                updateGameData(this)
            }
        }
    }
}