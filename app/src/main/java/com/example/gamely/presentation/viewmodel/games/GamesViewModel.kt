package com.example.gamely.presentation.viewmodel.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.example.gamely.domain.model.Game
import com.example.gamely.domain.usecase.GetGamesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class GamesViewModel(
    private val getGamesUseCase: GetGamesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GamesState())
    val uiState: StateFlow<GamesState> = _uiState.asStateFlow()

    private val actionFlow = MutableSharedFlow<GamesAction>()

    private val loadedGames = mutableListOf<Game>()

    private val basePagingFlow: Flow<PagingData<Game>> = getGamesUseCase
        .fetchGames()
        .cachedIn(viewModelScope)
        .map { pagingData ->
            pagingData.map { game ->
                if (!loadedGames.any { it.id == game.id }) {
                    loadedGames.add(game)
                }
                game
            }
        }

    val gamesFlow: Flow<PagingData<Game>> = _uiState.flatMapLatest { state ->
        if (state.searchQuery.isEmpty()) {
            basePagingFlow
        } else {
            val filteredGames = loadedGames.filter { game ->
                game.name.contains(state.searchQuery, ignoreCase = true)
            }
            flowOf(PagingData.from(filteredGames))
        }
    }

    init {
        handleAction()
    }

    fun sendAction(action: GamesAction) = viewModelScope.launch(Dispatchers.Main.immediate) {
        actionFlow.emit(action)
    }

    private fun handleAction() = viewModelScope.launch(Dispatchers.Main.immediate) {
        actionFlow.collect { action ->
            when (action) {
                is GamesAction.SearchGames -> searchGames(action.query)
                is GamesAction.ClearSearch -> clearSearch()
                is GamesAction.ToggleSearch -> toggleSearch()
            }
        }
    }

    private fun searchGames(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    private fun clearSearch() {
        _uiState.update { it.copy(searchQuery = "", isSearchActive = false) }
    }

    private fun toggleSearch() {
        val newState = !_uiState.value.isSearchActive
        _uiState.update { it.copy(isSearchActive = newState) }
        if (!newState) {
            clearSearch()
        }
    }
}
