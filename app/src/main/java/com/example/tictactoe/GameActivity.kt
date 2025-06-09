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
    //private lateinit var buttonMap: Map<Int, android.widget.Button>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityGameBinding.inflate(layoutInflater)

        enableEdgeToEdge()
        setContentView(binding.root)
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

//        buttonMap = mapOf(
//            0 to binding.btn0,
//            1 to binding.btn1,
//            2 to binding.btn2,
//            3 to binding.btn3,
//            4 to binding.btn4,
//            5 to binding.btn5,
//            6 to binding.btn6,
//            7 to binding.btn7,
//            8 to binding.btn8
//        )


        binding.startGameBtn.setOnClickListener{
            startGame()
        }

        GameData.gameModel.observe(this){
            gameModel=it
            setUI()
        }

    }
    fun setUI(){
        gameModel?.apply {
            binding.btn0.text = filledPos[0]
            binding.btn1.text = filledPos[1]
            binding.btn2.text = filledPos[2]
            binding.btn3.text = filledPos[3]
            binding.btn4.text = filledPos[4]
            binding.btn5.text = filledPos[5]
            binding.btn6.text = filledPos[6]
            binding.btn7.text = filledPos[7]
            binding.btn8.text = filledPos[8]


            binding.gameStatusText.text=
                when(gameStatus){
                    GameStatus.CREATED -> {
                        "Game ID : "+gameId
                    }
                    GameStatus.JOINED -> {
                        "click start"
                    }
                    GameStatus.INPROGRESS -> {
                        currentPlayer+"turn"
                    }
                    GameStatus.FINISHED -> {
                        if(winner.isNotEmpty()) winner +" won"
                        else "draw"
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
            if(gameStatus!=GameStatus.INPROGRESS){
                Toast.makeText(applicationContext,"Game Not Started",Toast.LENGTH_SHORT).show()
                return
            }
            //game in progress processing
            val clickedPos = (v?.tag as String).toInt()
            if(filledPos[clickedPos].isEmpty()){

                //improved Unique RULE Logic
//                if (moveHistory.size >= 6) {
//                    val oldestPos = moveHistory.removeAt(0)
//                    filledPos[oldestPos] = ""
//
//                    // Clear the corresponding button's text immediately
//                    val buttonId = resources.getIdentifier("btn_$oldestPos", "id", packageName)
//                    findViewById<View>(buttonId)?.let {
//                        if (it is android.widget.Button) {
//                            it.text = ""
//                        }
//                    }
//                }

                filledPos[clickedPos]=currentPlayer
                currentPlayer = if(currentPlayer=="X") "O" else "X"
                checkForWinner()
                updateGameData(this)
            }

        }

    }
}