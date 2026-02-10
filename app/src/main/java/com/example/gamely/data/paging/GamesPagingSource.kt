package com.example.gamely.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.gamely.domain.model.Game
import com.example.gamely.domain.repositories.GamesRepository

class GamesPagingSource(
    private val repository: GamesRepository
) : PagingSource<Int, Game>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Game> {
        val page = params.key ?: 1
        return repository.getGames(page).fold(
            onSuccess = { games ->
                LoadResult.Page(
                    data = games,
                    prevKey = if (page == 1) null else page - 1,
                    nextKey = if (games.isEmpty()) null else page + 1
                )
            },
            onFailure = { exception ->
                LoadResult.Error(exception)
            }
        )
    }

    override fun getRefreshKey(state: PagingState<Int, Game>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
