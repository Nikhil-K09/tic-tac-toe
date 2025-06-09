package com.example.tictactoe

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tictactoe.databinding.ActivityMainBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import kotlin.random.Random
import kotlin.random.nextInt

class MainActivity : AppCompatActivity() {

    lateinit var binding:ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding=ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.playOfflineBtn.setOnClickListener{
                createOfflineGame()
        }
        binding.createOnlineGameBtn.setOnClickListener{
            createOnlineGame()
        }

        binding.joinOnlineGameBtn.setOnClickListener{
            joinOnlineGame()
        }

        val rules = """
                    - Players take turns putting their marks in empty squares.
                    
                    - The first to get 3 in a row wins!
                    
                    Unique Rule -  
                    After 6 moves the oldest move will be highlighted
                        
                    When 7th move is made the oldest move disappears
                        
                    The highlighted move is not counted for winning
                        
                    NO MORE DRAWS!
                """.trimIndent()

        binding.btnShowRules.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Game Rules")
                .setMessage(rules)
                .setPositiveButton("Got it") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    fun createOfflineGame() {
        GameData.saveGameModel(
            GameModel(
                gameStatus = GameStatus.JOINED
            )
        )
        startGame()
    }
    fun createOnlineGame(){
        GameData.myID="X"
        GameData.saveGameModel(
            GameModel(
                gameStatus = GameStatus.CREATED,
                gameId = Random.nextInt(100000..999999).toString()
            )
        )
        startGame()
    }
    fun joinOnlineGame(){
        var gameId=binding.gameIdInput.text.toString()
        if(gameId.isEmpty()){
            binding.gameIdInput.setError("Enter Code!")
            return
        }
        GameData.myID="O"
        Firebase.firestore.collection("games")
            .document(gameId)
            .get()
            .addOnSuccessListener {
                val model = it?.toObject(GameModel::class.java)
                if(model==null){
                    binding.gameIdInput.setError("Enter Code!")
                }else{
                    model.gameStatus=GameStatus.JOINED
                    GameData.saveGameModel(model)
                    startGame()
                }
            }
    }

    fun startGame() {
        startActivity(Intent(this,GameActivity::class.java))
    }
}