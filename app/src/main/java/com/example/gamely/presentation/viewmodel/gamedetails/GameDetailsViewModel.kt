package com.example.gamely.presentation.viewmodel.gamedetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamely.domain.usecase.GetGameDetailsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameDetailsViewModel(
    private val getGameDetailsUseCase: GetGameDetailsUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(GameDetailsState())
    val uiState: StateFlow<GameDetailsState> = _uiState.asStateFlow()

    private val actionFlow = MutableSharedFlow<GameDetailsAction>()

    init {
        handleAction()
    }
    fun sendAction(action: GameDetailsAction) {
        viewModelScope.launch(Dispatchers.Main) {
            actionFlow.emit(action)
        }
    }
    private fun handleAction() {
        viewModelScope.launch(Dispatchers.Main) {
            actionFlow.collect { action ->
                when (action) {
                    is GameDetailsAction.LoadGameDetails -> loadGameDetails(action.gameId)
                    is GameDetailsAction.ToggleDescription -> toggleDescription()
                    is GameDetailsAction.RetryLoading -> retryLoading()
                }
            }
        }
    }

    private fun loadGameDetails(gameId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(gameId = gameId, isLoading = true, error = null) }

            _uiState.update { currentState ->
                getGameDetailsUseCase(gameId).fold(
                    onSuccess = { gameDetails ->
                        currentState.copy(
                            gameDetails = gameDetails,
                            isLoading = false,
                            error = null
                        )
                    },
                    onFailure = { exception ->
                        currentState.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to load game details"
                        )
                    }
                )
            }
        }
    }

    private fun toggleDescription() {
        _uiState.update { it.copy(isDescriptionExpanded = !it.isDescriptionExpanded) }
    }

    private fun retryLoading() {
        _uiState.value.gameId?.let { gameId ->
            sendAction(GameDetailsAction.LoadGameDetails(gameId))
        }
    }
}
