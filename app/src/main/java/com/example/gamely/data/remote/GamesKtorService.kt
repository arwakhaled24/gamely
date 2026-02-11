package com.example.gamely.data.remote

import com.example.gamely.BuildConfig
import com.example.gamely.data.dto.GameDetailsResponse
import com.example.gamely.data.dto.GamesResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class GamesKtorService(private val client: HttpClient) {
    suspend fun getGames(page: Int): Result<GamesResponse> {
        return try {
            val url = "${BuildConfig.BASE_URL}/${BuildConfig.GAMES_ENDPOINT}"
            val response: GamesResponse = client.get(url) {
                parameter(NetworkConstants.PARAM_KEY, BuildConfig.API_KEY)
                parameter(NetworkConstants.PAGE, page)
            }.body()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getGameDetails(gameId: Int): Result<GameDetailsResponse> {
        return try {
            val url = "${BuildConfig.BASE_URL}/${BuildConfig.GAME_DETAILS_ENDPOINT}/$gameId"
            val response: GameDetailsResponse = client.get(url) {
                parameter(NetworkConstants.PARAM_KEY, BuildConfig.API_KEY)
            }.body()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}