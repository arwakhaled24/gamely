package com.example.gamely.domain.usecase

import com.example.gamely.domain.model.GameDetails
import com.example.gamely.domain.repositories.GamesRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetGameDetailsUseCaseTest {

    private lateinit var gameRepository: GamesRepository
    private lateinit var useCase: GetGameDetailsUseCase

    @Before
    fun setup() {
        gameRepository = mockk()
        useCase = GetGameDetailsUseCase(gameRepository)
    }

    @Test
    fun `invoke returns success when repository returns success`() = runTest {
        val gameId = 1
        val expectedGameDetails = GameDetails(
            id = 1,
            name = "game name ",
            description = "game description",
            released = "2/2/2002",
            rating = 2.6,
            playtime = 8,
            backgroundImage = "kjhgajaldhhwbhnwsb",
            backgroundImageAdditional = "kjhgajaldhhwbhnwsb",
            genres = listOf("s", "w")
        )
        val expectedResult = Result.success(expectedGameDetails)
        coEvery { gameRepository.getGameDetails(gameId) } returns expectedResult


        val result = useCase(gameId)

        assertEquals(expectedResult, result)
        assert(result.isSuccess)
        assertEquals(expectedGameDetails, result.getOrNull())
    }


    @Test
    fun `invoke returns failure when repository returns failure`() = runTest {
        val gameId = 1
        val exception = Exception("network error")
        val expectedResult = Result.failure<GameDetails>(exception)
        coEvery { gameRepository.getGameDetails(gameId) } returns expectedResult


        val result = useCase(gameId)


        assert(result.isFailure)
        assertEquals(exception.message, result.exceptionOrNull()?.message)
        coVerify(exactly = 1) { gameRepository.getGameDetails(gameId) }
    }
}

