package com.example.tictactoe

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore

object GameData {
    private val _gameModel = MutableLiveData<GameModel>()
    val gameModel: LiveData<GameModel> = _gameModel
    var myID = ""

    fun saveGameModel(model: GameModel) {
        _gameModel.postValue(model)
        if (model.gameId != "-1") {
            FirebaseFirestore.getInstance().collection("games").document(model.gameId).set(model)
        }
    }

    fun fetchGameModel() {
        gameModel.value?.let { model ->
            if (model.gameId != "-1") {
                FirebaseFirestore.getInstance().collection("games")
                    .document(model.gameId)
                    .addSnapshotListener { snapshot, _ ->
                        val updatedModel = snapshot?.toObject(GameModel::class.java)
                        _gameModel.postValue(updatedModel)
                    }
            }
        }
    }
}
