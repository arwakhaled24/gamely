package com.example.gamely.presentation.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.gamely.R
import com.example.gamely.domain.model.Game
import com.example.gamely.presentation.composable.ErrorView
import com.example.gamely.presentation.composable.GamelySearchBar
import com.example.gamely.presentation.composable.GamesList
import com.example.gamely.presentation.composable.GamesTopAppBar
import com.example.gamely.presentation.composable.InitialLoadingIndicator
import com.example.gamely.presentation.viewmodel.games.GamesAction
import com.example.gamely.presentation.viewmodel.games.GamesState
import com.example.gamely.presentation.viewmodel.games.GamesViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamesScreen(
    viewModel: GamesViewModel = koinViewModel(),
    onGameClick: (Int) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lazyPagingItems = viewModel.gamesFlow.collectAsLazyPagingItems()

    GamesContent(
        uiState = uiState,
        lazyPagingItems = lazyPagingItems,
        onAction = viewModel::sendAction,
        onGameClick = onGameClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GamesContent(
    uiState: GamesState,
    lazyPagingItems: LazyPagingItems<Game>,
    onAction: (GamesAction) -> Unit,
    onGameClick: (Int) -> Unit
) {
    Scaffold(
        topBar = {
            GamesTopAppBar(
                isSearchActive = uiState.isSearchActive,
                onToggleSearch = { onAction(GamesAction.ToggleSearch) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isSearchActive) {
                GamelySearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = { onAction(GamesAction.SearchGames(it)) },
                    onClear = { onAction(GamesAction.ClearSearch) }
                )
            }
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    lazyPagingItems.loadState.refresh is LoadState.Loading -> {
                        InitialLoadingIndicator(
                            if (uiState.searchQuery.isEmpty()) stringResource(R.string.loading_games)
                            else stringResource(R.string.searching_for, uiState.searchQuery)
                        )
                    }

                    lazyPagingItems.loadState.refresh is LoadState.Error -> {
                        ErrorView(
                            onRetry = { lazyPagingItems.retry() }
                        )
                    }
                    else ->  GamesList(
                        lazyPagingItems = lazyPagingItems,
                        searchQuery = uiState.searchQuery,
                        onGameClick = onGameClick
                    )
                }
            }
        }
    }
}
