package com.blindspot.blindspotapi.backend.auth.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@ConfigurationProperties(prefix = "auth.jwt")
data class JwtProperties(
    val signingSecret: String = "",
    val accessTokenTtlMinutes: Long = 15,
    val refreshTokenTtlDays: Long = 30,
)

@ConfigurationProperties(prefix = "auth.google")
data class GoogleAuthProperties(
    val webClientId: String = "",
)

@Configuration
@EnableConfigurationProperties(JwtProperties::class, GoogleAuthProperties::class)
class AuthPropertiesConfig
