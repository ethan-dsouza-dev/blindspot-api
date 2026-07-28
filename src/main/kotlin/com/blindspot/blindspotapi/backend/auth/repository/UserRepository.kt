package com.blindspot.blindspotapi.backend.auth.repository

import com.blindspot.blindspotapi.backend.auth.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserRepository : JpaRepository<UserEntity, UUID> {
    fun findByGoogleSub(googleSub: String): UserEntity?
}
