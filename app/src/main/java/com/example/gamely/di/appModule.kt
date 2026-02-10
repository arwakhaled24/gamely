package com.example.gamely.di

import com.example.gamely.data.remote.GamesKtorService
import com.example.gamely.data.repositories.GameRepositoryImpl
import com.example.gamely.domain.repositories.GamesRepository
import com.example.gamely.domain.usecase.GetGameDetailsUseCase
import com.example.gamely.domain.usecase.GetGamesUseCase
import com.example.gamely.presentation.viewmodel.gamedetails.GameDetailsViewModel
import com.example.gamely.presentation.viewmodel.games.GamesViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                    }
                )
            }
            install(Logging) {
                level = LogLevel.INFO
            }
        }
    }
    single<GamesKtorService> {
        GamesKtorService(
            client = get(),
        )
    }
    single<GamesRepository> {
        GameRepositoryImpl(apiService = get())
    }
    single {
        GetGamesUseCase(
            gamesRepository = get()
        )
    }
    single {
        GetGameDetailsUseCase(
            gameRepository = get()
        )
    }
    viewModel { GamesViewModel(getGamesUseCase = get()) }
    viewModel { GameDetailsViewModel(getGameDetailsUseCase = get()) }
}