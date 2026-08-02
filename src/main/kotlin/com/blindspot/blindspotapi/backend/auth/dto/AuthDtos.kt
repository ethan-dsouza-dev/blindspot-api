package com.blindspot.blindspotapi.backend.auth.dto

import com.blindspot.blindspotapi.backend.auth.entity.UserEntity
import java.util.UUID

data class GoogleSignInRequest(
    val idToken: String,
)

data class RefreshRequest(
    val refreshToken: String,
)

data class SignOutRequest(
    val refreshToken: String,
)

data class UserDto(
    val id: UUID,
    val email: String,
    val name: String?,
    val pictureUrl: String?,
) {
    companion object {
        fun from(entity: UserEntity) = UserDto(
            id = entity.id,
            email = entity.email,
            name = entity.name,
            pictureUrl = entity.pictureUrl,
        )
    }
}

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val user: UserDto,
)
