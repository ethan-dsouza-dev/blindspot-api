package com.blindspot.blindspotapi.backend.auth

import com.blindspot.blindspotapi.backend.auth.entity.UserEntity
import com.blindspot.blindspotapi.backend.auth.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class UserService(
    private val userRepository: UserRepository,
) {
    @Transactional
    fun upsertFromGoogle(identity: GoogleIdentity): UserEntity {
        val existing = userRepository.findByGoogleSub(identity.sub)

        if (existing != null) {
            existing.email = identity.email
            existing.name = identity.name
            existing.pictureUrl = identity.pictureUrl
            existing.updatedAt = Instant.now()
            return userRepository.save(existing)
        }

        return userRepository.save(
            UserEntity(
                googleSub = identity.sub,
                email = identity.email,
                name = identity.name,
                pictureUrl = identity.pictureUrl,
            ),
        )
    }
}
