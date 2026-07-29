package com.blindspot.blindspotapi.backend.auth

import com.blindspot.blindspotapi.backend.auth.config.JwtProperties
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.crypto.SecretKey

/**
 * Issues and validates short-lived access JWTs signed with a symmetric secret
 * (`auth.jwt.signing-secret` / `JWT_SIGNING_SECRET`). Refresh tokens are handled separately by
 * [RefreshTokenService] and are opaque, DB-backed values rather than JWTs.
 */
@Service
class JwtService(
    private val jwtProperties: JwtProperties,
) {
    private val signingKey: SecretKey

    init {
        val secret = jwtProperties.signingSecret
        require(secret.length >= 32) {
            "auth.jwt.signing-secret (JWT_SIGNING_SECRET) must be set to a string of at least 32 characters"
        }
        signingKey = Keys.hmacShaKeyFor(secret.toByteArray())
    }

    data class AccessToken(val token: String, val expiresInSeconds: Long)

    fun generateAccessToken(userId: UUID): AccessToken {
        val now = Instant.now()
        val ttl = jwtProperties.accessTokenTtlMinutes
        val expiry = now.plus(ttl, ChronoUnit.MINUTES)

        val token = Jwts.builder()
            .subject(userId.toString())
            .issuedAt(java.util.Date.from(now))
            .expiration(java.util.Date.from(expiry))
            .signWith(signingKey)
            .compact()

        return AccessToken(token, ttl * 60)
    }

    /** Returns the authenticated user id, or throws a 401 if the token is missing/invalid/expired. */
    fun validateAccessToken(token: String): UUID {
        try {
            val claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .payload

            return UUID.fromString(claims.subject)
        } catch (e: ExpiredJwtException) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Access token expired")
        } catch (e: JwtException) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid access token")
        } catch (e: IllegalArgumentException) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid access token")
        }
    }
}
