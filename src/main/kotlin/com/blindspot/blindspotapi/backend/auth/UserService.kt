package com.blindspot.blindspotapi.backend.auth

import com.blindspot.blindspotapi.backend.auth.entity.UserEntity
import com.blindspot.blindspotapi.backend.auth.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class UserService(
    private val userRepository: UserRepository,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun upsertFromGoogle(identity: GoogleIdentity): UserEntity {
        logger.info("Upserting user from Google identity: sub={}, email={}", identity.sub, identity.email)
        val existing = userRepository.findByGoogleSub(identity.sub)

        return if (existing != null) {
            existing.email = identity.email
            existing.name = identity.name
            existing.pictureUrl = identity.pictureUrl
            existing.updatedAt = Instant.now()
            logger.info("Updating existing user: id={}, sub={}", existing.id, existing.googleSub)
            userRepository.save(existing)
        } else {
            logger.info("Creating new user for sub={}", identity.sub)
            userRepository.save(
                UserEntity(
                    googleSub = identity.sub,
                    email = identity.email,
                    name = identity.name,
                    pictureUrl = identity.pictureUrl,
                ),
            )
        }
    }
}
