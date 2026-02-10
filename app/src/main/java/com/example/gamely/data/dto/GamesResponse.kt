package com.example.gamely.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GamesResponse(
    @SerialName("count")
    val count: Int? = 0,
    @SerialName("next")
    val next: String? ="",
    @SerialName("previous")
    val previous: String? ="",
    @SerialName("results")
    val results: List<GameDto> = emptyList()
)