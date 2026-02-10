package com.example.gamely.data.dto

import com.example.gamely.domain.model.Game
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GameDto(
    @SerialName("id")
    val id: Int? = 0,
    @SerialName("name")
    val name: String? = "",
    @SerialName("background_image")
    val backgroundImage: String? = "",
    @SerialName("rating")
    val rating: Double = 0.0
) {
    fun toGame(): Game {
        return Game(
            id = id ?: 0,
            name = name ?: "",
            imageUrl = backgroundImage,
            rating = rating
        )
    }
}