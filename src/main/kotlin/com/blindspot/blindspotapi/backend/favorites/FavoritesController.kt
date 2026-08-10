package com.blindspot.blindspotapi.backend.favorites

import com.blindspot.blindspotapi.backend.auth.repository.UserRepository
import com.blindspot.blindspotapi.backend.favorites.dto.FavoriteRequest
import com.blindspot.blindspotapi.backend.favorites.dto.FavoritesResponse
import com.blindspot.blindspotapi.backend.favorites.entity.FavoriteEntity
import com.blindspot.blindspotapi.backend.favorites.repository.FavoriteRepository
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/favorites")
class FavoritesController(
    private val favoriteRepository: FavoriteRepository,
    private val userRepository: UserRepository,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping
    fun getFavorites(authentication: Authentication): FavoritesResponse {
        val userId = authentication.userId()
        val placeIds = favoriteRepository.findAllByUserId(userId).map { it.placeId }
        return FavoritesResponse(placeIds)
    }

    @PostMapping
    fun addFavorite(
        authentication: Authentication,
        @RequestBody request: FavoriteRequest,
    ): ResponseEntity<Unit> {
        val userId = authentication.userId()
        if (!favoriteRepository.existsByUserIdAndPlaceId(userId, request.placeId)) {
            val user = userRepository.getReferenceById(userId)
            favoriteRepository.save(FavoriteEntity(user = user, placeId = request.placeId))
            logger.info("Added favorite: userId={}, placeId={}", userId, request.placeId)
        }
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @DeleteMapping("/{placeId}")
    fun removeFavorite(
        authentication: Authentication,
        @PathVariable placeId: String,
    ): ResponseEntity<Unit> {
        val userId = authentication.userId()
        favoriteRepository.deleteByUserIdAndPlaceId(userId, placeId)
        logger.info("Removed favorite: userId={}, placeId={}", userId, placeId)
        return ResponseEntity.noContent().build()
    }

    private fun Authentication.userId(): UUID = principal as UUID
}