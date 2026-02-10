package com.example.gamely.presentation.viewmodel.gamedetails

sealed interface GameDetailsAction {
    data class LoadGameDetails(val gameId: Int) : GameDetailsAction
    data object ToggleDescription : GameDetailsAction
    data object RetryLoading : GameDetailsAction
}
