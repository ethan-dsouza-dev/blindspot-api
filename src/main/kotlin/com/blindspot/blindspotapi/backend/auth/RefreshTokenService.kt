package com.blindspot.blindspotapi.backend.auth

import com.blindspot.blindspotapi.backend.auth.config.JwtProperties
import com.blindspot.blindspotapi.backend.auth.entity.RefreshTokenEntity
import com.blindspot.blindspotapi.backend.auth.entity.UserEntity
import com.blindspot.blindspotapi.backend.auth.repository.RefreshTokenRepository
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Base64
import java.util.UUID

/**
 * Generates opaque refresh tokens and persists only their SHA-256 hash, so a database leak never
 * exposes a usable token. Each successful refresh rotates the token (old one revoked, new one
 * issued) to limit the blast radius of a stolen refresh token.
 */
@Service
class RefreshTokenService(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtProperties: JwtProperties,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val secureRandom = SecureRandom()

    data class IssuedRefreshToken(val rawToken: String, val expiresAt: Instant)

    @Transactional
    fun issue(user: UserEntity): IssuedRefreshToken {
        val rawToken = generateRawToken()
        val expiresAt = Instant.now().plus(jwtProperties.refreshTokenTtlDays, ChronoUnit.DAYS)

        val saved = refreshTokenRepository.save(
            RefreshTokenEntity(
                user = user,
                tokenHash = hash(rawToken),
                expiresAt = expiresAt,
            ),
        )

        logger.info("Issued refresh token for userId={}, tokenId={}", user.id, saved.id)
        enforceCap(user, saved.id)
        return IssuedRefreshToken(rawToken, expiresAt)
    }

    /**
     * Validates [rawToken], revokes it, and returns the owning user so a new pair can be issued.
     * Throws 401 if the token is unknown, expired, or already revoked/used.
     */
    @Transactional
    fun consume(rawToken: String): UserEntity {
        val tokenHash = hash(rawToken)
        val entity = refreshTokenRepository.findByTokenHash(tokenHash)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token").also {
                logger.warn("Refresh token not found in database")
            }

        if (!entity.isValid()) {
            logger.warn("Refresh token is expired or already revoked for userId={}", entity.user.id)
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expired or revoked")
        }

        entity.revokedAt = Instant.now()
        refreshTokenRepository.save(entity)
        logger.info("Consumed refresh token for userId={}, tokenId={}", entity.user.id, entity.id)

        return entity.user
    }

    private fun enforceCap(user: UserEntity, currentTokenId: UUID) {
        val max = jwtProperties.maxActiveRefreshTokensPerUser.toInt()
        if (max <= 0) return

        val active = refreshTokenRepository.findActiveByUser(user, Instant.now())
        val overflow = active.size - max
        if (overflow <= 0) return

        val toDelete = active.filter { it.id != currentTokenId }.take(overflow)
        if (toDelete.isNotEmpty()) {
            refreshTokenRepository.deleteAll(toDelete)
        }
    }

    private fun generateRawToken(): String {
        val bytes = ByteArray(32)
        secureRandom.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    private fun hash(rawToken: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(rawToken.toByteArray())
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest)
    }
}
