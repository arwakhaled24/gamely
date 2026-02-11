package com.example.gamely.presentation.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.example.gamely.R
import com.example.gamely.domain.model.Game

@Composable
fun GamesList(
    lazyPagingItems: LazyPagingItems<Game>,
    searchQuery: String,
    onGameClick: (Int) -> Unit
) {
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
                GameCard(game = game, onClick = { onGameClick(game.id) })
            }
        }

        when (lazyPagingItems.loadState.append) {
            is LoadState.Loading -> item { PaginationLoadingIndicator() }
            is LoadState.Error -> item { ErrorView(onRetry = lazyPagingItems::retry) }
            is LoadState.NotLoading -> {
                if (lazyPagingItems.loadState.append.endOfPaginationReached &&
                    lazyPagingItems.itemCount > 0
                ) {
                    item { InfoMessage(texts = listOf(stringResource(R.string.no_more_games))) }
                }
            }
        }

        if (lazyPagingItems.itemCount == 0 &&
            lazyPagingItems.loadState.refresh is LoadState.NotLoading &&
            searchQuery.isNotEmpty()
        ) {
            item {
                InfoMessage(
                    texts = listOf(
                        stringResource(R.string.no_games_found_for, searchQuery),
                        stringResource(R.string.clear_search_and_scroll_to_load_more_games)
                    )
                )
            }
        }
    }
}
