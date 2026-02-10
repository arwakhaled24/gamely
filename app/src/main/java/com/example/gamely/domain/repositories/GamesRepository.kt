package com.example.gamely.domain.repositories

import androidx.paging.PagingData
import com.example.gamely.domain.model.Game
import com.example.gamely.domain.model.GameDetails
import kotlinx.coroutines.flow.Flow

interface GamesRepository {
    suspend fun getGames(page: Int): Result<List<Game>>
    
    fun getGamesPaginated(
        pageSize: Int,
        enablePlaceholders: Boolean,
        prefetchDistance: Int,
        initialLoadSize: Int,
        maxCacheSize: Int
    ): Flow<PagingData<Game>>
    
    suspend fun getGameDetails(id: Int): Result<GameDetails>
}
