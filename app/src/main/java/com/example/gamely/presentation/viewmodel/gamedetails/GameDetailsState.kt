package com.example.gamely.presentation.viewmodel.gamedetails

import com.example.gamely.domain.model.GameDetails

data class GameDetailsState(
    val gameId: Int? = null,
    val gameDetails: GameDetails? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isDescriptionExpanded: Boolean = false
)
