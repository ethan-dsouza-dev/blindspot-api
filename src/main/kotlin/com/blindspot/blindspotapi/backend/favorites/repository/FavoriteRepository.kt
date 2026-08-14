package com.blindspot.blindspotapi.backend.favorites.repository

import com.blindspot.blindspotapi.backend.favorites.entity.FavoriteEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

interface FavoriteRepository : JpaRepository<FavoriteEntity, UUID> {

    fun findAllByUserId(userId: UUID): List<FavoriteEntity>

    fun existsByUserIdAndPlaceId(userId: UUID, placeId: String): Boolean

    @Transactional
    fun deleteByUserIdAndPlaceId(userId: UUID, placeId: String)
}