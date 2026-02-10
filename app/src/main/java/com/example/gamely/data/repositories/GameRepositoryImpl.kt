package com.example.gamely.data.repositories

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.gamely.data.paging.GamesPagingSource
import com.example.gamely.data.remote.GamesKtorService
import com.example.gamely.domain.model.Game
import com.example.gamely.domain.model.GameDetails
import com.example.gamely.domain.repositories.GamesRepository
import kotlinx.coroutines.flow.Flow

class GameRepositoryImpl(private val apiService: GamesKtorService) : GamesRepository {

    override suspend fun getGames(page: Int): Result<List<Game>> {
        return apiService.getGames(page).mapCatching { response ->
            response.results.map { it.toGame() }
        }
    }

    override fun getGamesPaginated(
        pageSize: Int,
        enablePlaceholders: Boolean,
        prefetchDistance: Int,
        initialLoadSize: Int,
        maxCacheSize: Int
    ): Flow<PagingData<Game>> {
        return Pager(
            config = PagingConfig(
                pageSize = pageSize,
                enablePlaceholders = enablePlaceholders,
                prefetchDistance = prefetchDistance,
                initialLoadSize = initialLoadSize,
                maxSize = maxCacheSize
            ),
            pagingSourceFactory = {
                GamesPagingSource(repository = this)
            }
        ).flow
    }

    override suspend fun getGameDetails(id: Int): Result<GameDetails> {
        return apiService.getGameDetails(id).mapCatching { response ->
            response.toGameDetails()
        }
    }
}