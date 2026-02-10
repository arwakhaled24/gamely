package com.example.themotherofgames.presentation.viewmodel.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.themotherofgames.domain.usecase.GetGamesUseCase
import com.example.themotherofgames.presentation.util.Paginator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GamesViewModel(
    private val getGamesUseCase: GetGamesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GamesState())
    val uiState: StateFlow<GamesState> = _uiState.asStateFlow()

    private val paginator = Paginator(
        scope = viewModelScope,
        initialKey = 1,
        onRequest = { page -> getGamesUseCase(page = page) },
        onSuccess = { items, _ ->
            _uiState.update { it.copy(games = it.games + items) }
        }
    )

    init {
        viewModelScope.launch {
            paginator.state.collect { paginationState ->
                _uiState.update { it.copy(paginationState = paginationState) }
            }
        }
        paginator.loadNextItems()
    }

    fun sendAction(action: GamesAction) {
        when (action) {
            GamesAction.LoadMoreGames -> paginator.loadNextItems()
            GamesAction.RetryLoading -> paginator.retry()
        }
    }
}