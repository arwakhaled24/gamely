package com.example.gamely.presentation.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gamely.presentation.compossable.ErrorView
import com.example.gamely.presentation.compossable.GameDetailsContent
import com.example.gamely.presentation.compossable.InitialLoadingIndicator
import com.example.gamely.presentation.viewmodel.gamedetails.GameDetailsAction
import com.example.gamely.presentation.viewmodel.gamedetails.GameDetailsViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun GameDetailsScreen(
    gameId: Int,
    onBack: () -> Unit,
    viewModel: GameDetailsViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(gameId) {
        viewModel.initialize(gameId)
    }
    Box(modifier = Modifier.fillMaxSize()) {
        when {
            state.isLoading -> {
                InitialLoadingIndicator(text = "Loading Game Details..")
            }
            state.error != null && state.gameDetails == null -> {
                ErrorView(
                    message = state.error ?: "Error loading game details",
                    onRetry = { viewModel.sendAction(GameDetailsAction.RetryLoading) }
                )
            }
            state.gameDetails != null -> {
                GameDetailsContent(
                    state = state,
                    onBack = onBack,
                    onToggleDescription = { 
                        viewModel.sendAction(GameDetailsAction.ToggleDescription)
                    }
                )
            }
        }
    }
}




