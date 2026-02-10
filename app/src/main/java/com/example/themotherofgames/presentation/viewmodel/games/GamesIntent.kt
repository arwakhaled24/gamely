package com.example.themotherofgames.presentation.viewmodel.games

sealed interface GamesAction {
    data object LoadMoreGames : GamesAction
    data object RetryLoading : GamesAction
}