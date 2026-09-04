package com.blindspot.blindspotapi.backend.favorites.dto

data class FavoriteRequest(
    val placeId: String,
)

data class FavoritesResponse(
    val placeIds: List<String>,
)