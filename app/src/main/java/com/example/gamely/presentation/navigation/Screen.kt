package com.example.gamely.presentation.navigation

sealed class Screen(val route: String) {
    data object Games : Screen("games")
    data object GameDetails : Screen("game_details/{gameId}") {
        fun createRoute(gameId: Int) = "game_details/$gameId"
    }
}
