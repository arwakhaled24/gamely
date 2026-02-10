package com.example.themotherofgames.domain.repositories

import com.example.themotherofgames.domain.model.Game

interface GamesRepository {
    suspend fun getGames(page: Int): Result<List<Game>>
}
