package com.example.gamely.data.dto

import com.example.gamely.domain.model.GameDetails
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GameDetailsResponse(
    @SerialName("id")
    val id: Int? = 0,

    @SerialName("name")
    val name: String? = "",

    @SerialName("name_original")
    val nameOriginal: String? = "",

    @SerialName("description")
    val description: String? = "",

    @SerialName("description_raw")
    val descriptionRaw: String? = "",

    @SerialName("released")
    val released: String? = "",

    @SerialName("background_image")
    val backgroundImage: String? = "",

    @SerialName("background_image_additional")
    val backgroundImageAdditional: String? = "",

    @SerialName("rating")
    val rating: Double? = 0.0,

    @SerialName("playtime")
    val playtime: Int? = 0,

    @SerialName("genres")
    val genres: List<GenreDto>? = emptyList()
){

    fun toGameDetails(): GameDetails {
        return GameDetails(
            id = id ?: 0,
            name = name ?: "Unknown",
            description = description ?: descriptionRaw ?: "No description available",
            released = released ?: "Unknown",
            rating = rating ?: 0.0,
            playtime = playtime ?: 0,
            backgroundImage = backgroundImage ?: "",
            backgroundImageAdditional = backgroundImageAdditional,
            genres = genres?.map { it.name } ?: emptyList()
        )
    }
}


