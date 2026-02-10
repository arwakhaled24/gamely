package com.example.themotherofgames.data.remote

import com.example.themotherofgames.data.dto.GamesResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class GamesKtorService(private val client: HttpClient) {
    companion object {
        private const val BASE_URL = "https://api.rawg.io/api"
        private const val GAMES_ENDPOINT = "$BASE_URL/games"

    }

     suspend fun getGames(page: Int): GamesResponse {
        return client.get(GAMES_ENDPOINT) {
            parameter("key", "47e76ee2cca6467ba274d212fe930837")
            parameter("page", page)
        }.body()
    }
}