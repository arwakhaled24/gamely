package com.example.gamely.domain.usecase

import com.example.gamely.domain.model.GameDetails
import com.example.gamely.domain.repositories.GamesRepository

class GetGameDetailsUseCase(private val gameRepository: GamesRepository) {
    suspend operator fun invoke(id: Int): Result<GameDetails> {
        return gameRepository.getGameDetails(id)
    }
}