package com.blindspot.blindspotapi.backend.auth

import com.blindspot.blindspotapi.backend.auth.dto.AuthResponse
import com.blindspot.blindspotapi.backend.auth.dto.GoogleSignInRequest
import com.blindspot.blindspotapi.backend.auth.dto.RefreshRequest
import com.blindspot.blindspotapi.backend.auth.dto.UserDto
import com.blindspot.blindspotapi.backend.auth.entity.UserEntity
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val googleTokenVerifier: GoogleTokenVerifier,
    private val userService: UserService,
    private val jwtService: JwtService,
    private val refreshTokenService: RefreshTokenService,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @PostMapping("/google")
    fun signInWithGoogle(@RequestBody request: GoogleSignInRequest): AuthResponse {
        logger.info("Received Google sign-in request")
        val identity = googleTokenVerifier.verify(request.idToken)
        val user = userService.upsertFromGoogle(identity)
        logger.info("Issuing token pair for userId={}, email={}", user.id, user.email)
        return issueTokenPair(user)
    }

    @PostMapping("/refresh")
    fun refresh(@RequestBody request: RefreshRequest): AuthResponse {
        logger.info("Received refresh token request")
        val user = refreshTokenService.consume(request.refreshToken)
        logger.info("Issuing refreshed token pair for userId={}", user.id)
        return issueTokenPair(user)
    }

    private fun issueTokenPair(user: UserEntity): AuthResponse {
        val accessToken = jwtService.generateAccessToken(user.id)
        val refreshToken = refreshTokenService.issue(user)

        return AuthResponse(
            accessToken = accessToken.token,
            refreshToken = refreshToken.rawToken,
            expiresIn = accessToken.expiresInSeconds,
            user = UserDto.from(user),
        ).also {
            logger.info("Issued token pair: userId={}, expiresIn={}", user.id, accessToken.expiresInSeconds)
        }
    }
}
