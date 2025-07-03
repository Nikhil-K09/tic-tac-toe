package com.example.tictactoe

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tictactoe.databinding.ActivityMainBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlin.random.Random
import kotlin.random.nextInt

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.playOfflineBtn.setOnClickListener { createOfflineGame() }
        binding.createOnlineGameBtn.setOnClickListener { createOnlineGame() }
        binding.joinOnlineGameBtn.setOnClickListener { joinOnlineGame() }

        val rules = """
            - Players take turns putting their marks in empty squares.
            - The first to get 3 in a row wins!
            
            Unique Rule:
            After 6 moves the oldest move will be highlighted.
            On the 7th move, the oldest disappears.
            Highlighted move is not counted for winning.
        """.trimIndent()

        binding.btnShowRules.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Game Rules")
                .setMessage(rules)
                .setPositiveButton("Got it") { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }

    private fun createOfflineGame() {
        GameData.saveGameModel(
            GameModel(gameStatus = GameStatus.JOINED)
        )
        startGame()
    }

    private fun createOnlineGame() {
        GameData.myID = "X"
        val gameId = Random.nextInt(100000..999999).toString()
        GameData.saveGameModel(
            GameModel(
                gameStatus = GameStatus.CREATED,
                gameId = gameId
            )
        )
        startGame()
    }

    private fun joinOnlineGame() {
        val gameId = binding.gameIdInput.text.toString()
        if (gameId.isEmpty()) {
            binding.gameIdInput.error = "Enter Code!"
            return
        }
        GameData.myID = "O"
        FirebaseFirestore.getInstance().collection("games")
            .document(gameId)
            .get()
            .addOnSuccessListener {
                val model = it?.toObject<GameModel>()
                if (model == null) {
                    binding.gameIdInput.error = "Game not found!"
                } else {
                    model.gameStatus = GameStatus.JOINED
                    GameData.saveGameModel(model)
                    startGame()
                }
            }
    }

    private fun startGame() {
        startActivity(Intent(this, GameActivity::class.java))
    }
}
