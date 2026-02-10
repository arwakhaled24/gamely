package com.example.gamely.domain.model

data class Game(
    val id: Int,
    val name: String,
    val imageUrl: String?,
    val rating: Double
)