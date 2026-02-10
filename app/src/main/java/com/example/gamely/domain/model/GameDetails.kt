package com.example.gamely.domain.model

data class GameDetails(
    val id: Int,
    val name: String,
    val description: String,
    val released: String,
    val rating: Double,
    val playtime: Int,
    val backgroundImage: String,
    val backgroundImageAdditional: String?,
    val genres: List<String>
)