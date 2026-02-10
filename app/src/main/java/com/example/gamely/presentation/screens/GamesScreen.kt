package com.example.gamely.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.example.gamely.domain.model.Game
import com.example.gamely.presentation.compossable.ErrorView
import com.example.gamely.presentation.compossable.GameCard
import com.example.gamely.presentation.compossable.InitialLoadingIndicator
import com.example.gamely.presentation.compossable.PaginationLoadingIndicator
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
            TopAppBar(
                title = { Text("Gamely") },
                actions = {
                    IconButton(onClick = { onAction(GamesAction.ToggleSearch) }) {
                        Icon(
                            imageVector = if (uiState.isSearchActive) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = if (uiState.isSearchActive) "Close search" else "Search"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isSearchActive) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { onAction(GamesAction.SearchGames(it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Search games...") },
                    singleLine = true,
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onAction(GamesAction.ClearSearch) }) {
                                Icon(Icons.Default.Close, "Clear search")
                            }
                        }
                    }
                )
            }
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    lazyPagingItems.loadState.refresh is LoadState.Loading -> {
                        InitialLoadingIndicator(
                            if (uiState.searchQuery.isEmpty()) "Loading Games..."
                            else "Searching for \"${uiState.searchQuery}\"..."
                        )
                    }

                    lazyPagingItems.loadState.refresh is LoadState.Error -> {
                        val error = lazyPagingItems.loadState.refresh as LoadState.Error
                        ErrorView(
                            message = error.error.message ?: "Error loading games",
                            onRetry = { lazyPagingItems.retry() }
                        )
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                count = lazyPagingItems.itemCount,
                                key = lazyPagingItems.itemKey { it.id }
                            ) { index ->
                                lazyPagingItems[index]?.let { game ->
                                    GameCard(
                                        game = game,
                                        onClick = { onGameClick(game.id) }
                                    )
                                }
                            }
                            when (lazyPagingItems.loadState.append) {
                                is LoadState.Loading -> {
                                    item {
                                        PaginationLoadingIndicator()
                                    }
                                }

                                is LoadState.Error -> {
                                    val error = lazyPagingItems.loadState.append as LoadState.Error
                                    item {
                                        ErrorView(
                                            message = error.error.message ?: "Error loading more",
                                            onRetry = { lazyPagingItems.retry() }
                                        )
                                    }
                                }

                                is LoadState.NotLoading -> {
                                    if (lazyPagingItems.loadState.append.endOfPaginationReached && lazyPagingItems.itemCount > 0) {
                                        item {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    "No more games",
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            if (lazyPagingItems.itemCount == 0 &&
                                lazyPagingItems.loadState.refresh is LoadState.NotLoading &&
                                uiState.searchQuery.isNotEmpty()
                            ) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                "No games found for \"${uiState.searchQuery}\"",
                                            )
                                            Text("Clear search and scroll to load more games")
                                        }
                                    }
                                }
                            }

                            if (lazyPagingItems.itemCount > 0 &&
                                lazyPagingItems.loadState.append.endOfPaginationReached &&
                                uiState.searchQuery.isNotEmpty()
                            ) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text("End of search results",)
                                            Text("Clear search to load more games")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
