package com.example.themotherofgames.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GameDto(
    @SerialName("id")
    val id: Int,
    @SerialName("name")
    val name: String,
    @SerialName("background_image")
    val backgroundImage: String? = null,
    @SerialName("rating")
    val rating: Double = 0.0
)