package com.example.themotherofgames.data.repositories

import com.example.themotherofgames.data.dto.toDomain
import com.example.themotherofgames.data.remote.GamesKtorService
import com.example.themotherofgames.domain.model.Game
import com.example.themotherofgames.domain.repositories.GamesRepository

class GameRepositoryImpl(private val apiService: GamesKtorService) : GamesRepository {
    override suspend fun getGames(page: Int): Result<List<Game>> {
        val response = apiService.getGames(page)
        return Result.success(response.results.toDomain())
    }
}