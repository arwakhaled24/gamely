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

    override fun getGamesPaginated(): Flow<PagingData<Game>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                prefetchDistance = 10,
                initialLoadSize = 20,
                maxSize = 200
            ),
            pagingSourceFactory = { GamesPagingSource(apiService) }
        ).flow
    }

    override suspend fun getGameDetails(id: Int): Result<GameDetails> {
        return apiService.getGameDetails(id).mapCatching { response ->
            response.toGameDetails()
        }
    }
}