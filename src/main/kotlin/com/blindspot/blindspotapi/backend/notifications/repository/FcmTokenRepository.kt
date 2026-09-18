package com.blindspot.blindspotapi.backend.notifications.repository

import com.blindspot.blindspotapi.backend.notifications.entity.FcmTokenEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface FcmTokenRepository : JpaRepository<FcmTokenEntity, UUID> {
    fun findByToken(token: String): FcmTokenEntity?

    fun findAllByUserId(userId: UUID): List<FcmTokenEntity>

    fun deleteByUserIdAndToken(userId: UUID, token: String): Long

    fun deleteAllByUserId(userId: UUID): Long
}
