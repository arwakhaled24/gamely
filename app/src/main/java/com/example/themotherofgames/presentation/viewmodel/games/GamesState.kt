package com.example.themotherofgames.presentation.viewmodel.games

import com.example.themotherofgames.domain.model.Game
import com.example.themotherofgames.presentation.util.PaginationState

data class GamesState(
    val games: List<Game> = emptyList(),
    val paginationState: PaginationState = PaginationState.Idle
) {
    val isInitialLoading: Boolean
        get() = paginationState is PaginationState.InitialLoading
    
    val isPaginationLoading: Boolean
        get() = paginationState is PaginationState.PaginationLoading
    
    val error: String?
        get() = (paginationState as? PaginationState.Error)?.throwable?.message
    
    val canLoadMore: Boolean
        get() = paginationState is PaginationState.Idle
}