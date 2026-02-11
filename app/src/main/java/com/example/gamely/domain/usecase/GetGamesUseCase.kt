package com.example.gamely.domain.usecase

import androidx.paging.PagingData
import com.example.gamely.domain.model.Game
import com.example.gamely.domain.repositories.GamesRepository
import kotlinx.coroutines.flow.Flow

class GetGamesUseCase(private val gamesRepository: GamesRepository) {
    fun fetchGames(): Flow<PagingData<Game>> {
        return gamesRepository.getGamesPaginated()
    }
}