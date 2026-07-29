package com.blindspot.blindspotapi.backend.auth.repository

import com.blindspot.blindspotapi.backend.auth.entity.RefreshTokenEntity
import com.blindspot.blindspotapi.backend.auth.entity.UserEntity
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface RefreshTokenRepository : JpaRepository<RefreshTokenEntity, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findByTokenHash(tokenHash: String): RefreshTokenEntity?

    @Query("SELECT rt FROM RefreshTokenEntity rt WHERE rt.user = :user AND rt.revokedAt IS NULL AND rt.expiresAt > :now ORDER BY rt.createdAt ASC")
    fun findActiveByUser(@Param("user") user: UserEntity, @Param("now") now: Instant): List<RefreshTokenEntity>
}
