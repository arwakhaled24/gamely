package com.example.themotherofgames.data.dto

import com.example.themotherofgames.domain.model.Game

fun GameDto.toDomain(): Game {
    return Game(
        id = id,
        name = name,
        imageUrl = backgroundImage,
        rating = rating
    )
}

fun List<GameDto>.toDomain(): List<Game> {
    return map { it.toDomain() }
}