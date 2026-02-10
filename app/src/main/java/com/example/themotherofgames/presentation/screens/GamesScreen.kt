package com.example.themotherofgames.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.themotherofgames.presentation.compossable.ErrorView
import com.example.themotherofgames.presentation.compossable.GameCard
import com.example.themotherofgames.presentation.compossable.InitialLoadingIndicator
import com.example.themotherofgames.presentation.compossable.PaginationLoadingIndicator
import com.example.themotherofgames.presentation.util.PaginationState
import com.example.themotherofgames.presentation.viewmodel.games.GamesAction
import com.example.themotherofgames.presentation.viewmodel.games.GamesState
import com.example.themotherofgames.presentation.viewmodel.games.GamesViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamesScreen(viewModel: GamesViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("THE MOTHER OF GAMED") },
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isInitialLoading -> InitialLoadingIndicator()
                state.paginationState is PaginationState.Error && state.games.isEmpty() -> {
                    ErrorView(
                        message = state.error ?: "Error loading games",
                        onRetry = { viewModel.sendAction(GamesAction.RetryLoading) }
                    )
                }
                else -> GamesList(state, viewModel)
            }
        }
    }
}

@Composable
private fun GamesList(
    state: GamesState,
    viewModel: GamesViewModel // need to be hof
) {
    val listState = rememberLazyListState()

    LaunchedEffect(listState) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= totalItems - 3
        }
            .distinctUntilChanged()
            .collect { shouldLoad ->
                if (shouldLoad && state.canLoadMore) {
                    viewModel.sendAction(GamesAction.LoadMoreGames)
                }
            }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items = state.games, key = { it.id }) { game ->
            GameCard(game = game)
        }

        if (state.isPaginationLoading) {
            item(key = "loading") {
                PaginationLoadingIndicator()
            }
        }

        if (state.paginationState is PaginationState.Error) {
            item(key = "error") {
                ErrorView(
                    message = state.error ?: "Error loading more",
                    onRetry = { viewModel.sendAction(GamesAction.RetryLoading) }
                )
            }
        }

        if (state.paginationState is PaginationState.EndReached) {
            item(key = "end") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No more games", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}