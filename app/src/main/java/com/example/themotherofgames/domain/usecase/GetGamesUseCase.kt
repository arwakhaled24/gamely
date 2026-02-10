package com.example.themotherofgames.domain.usecase

import com.example.themotherofgames.domain.model.Game
import com.example.themotherofgames.domain.repositories.GamesRepository

class GetGamesUseCase (private val gamesRepository: GamesRepository) {
    suspend operator fun invoke(page: Int): Result<List<Game>> {
        return gamesRepository.getGames(page)

    }
}