package com.blindspot.blindspotapi.backend.auth

import com.blindspot.blindspotapi.backend.auth.config.JwtProperties
import com.blindspot.blindspotapi.backend.auth.entity.RefreshTokenEntity
import com.blindspot.blindspotapi.backend.auth.entity.UserEntity
import com.blindspot.blindspotapi.backend.auth.repository.RefreshTokenRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Base64

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
    private val secureRandom = SecureRandom()

    data class IssuedRefreshToken(val rawToken: String, val expiresAt: Instant)

    @Transactional
    fun issue(user: UserEntity): IssuedRefreshToken {
        val rawToken = generateRawToken()
        val expiresAt = Instant.now().plus(jwtProperties.refreshTokenTtlDays, ChronoUnit.DAYS)

        refreshTokenRepository.save(
            RefreshTokenEntity(
                user = user,
                tokenHash = hash(rawToken),
                expiresAt = expiresAt,
            ),
        )

        return IssuedRefreshToken(rawToken, expiresAt)
    }

    /**
     * Validates [rawToken], revokes it, and returns the owning user so a new pair can be issued.
     * Throws 401 if the token is unknown, expired, or already revoked/used.
     */
    @Transactional
    fun consume(rawToken: String): UserEntity {
        val entity = refreshTokenRepository.findByTokenHash(hash(rawToken))
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token")

        if (!entity.isValid()) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expired or revoked")
        }

        entity.revokedAt = Instant.now()
        refreshTokenRepository.save(entity)

        return entity.user
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
