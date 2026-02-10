package com.example.gamely.presentation.viewmodel.games

sealed class GamesAction {
    data class SearchGames(val query: String) : GamesAction()
    object ClearSearch : GamesAction()
    object ToggleSearch : GamesAction()
}
