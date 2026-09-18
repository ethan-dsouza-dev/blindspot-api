package com.blindspot.blindspotapi.backend.notifications

import com.blindspot.blindspotapi.backend.auth.repository.UserRepository
import com.blindspot.blindspotapi.backend.notifications.dto.FcmTokenRequest
import com.blindspot.blindspotapi.backend.notifications.repository.FcmTokenRepository
import com.blindspot.blindspotapi.backend.notifications.entity.FcmTokenEntity
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.util.UUID

@RestController
@RequestMapping("/api/notifications")
class NotificationsController(
    private val fcmTokenRepository: FcmTokenRepository,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @PostMapping("/fcm-token")
    @Transactional
    fun registerToken(
        authentication: Authentication,
        @RequestBody request: FcmTokenRequest,
    ): ResponseEntity<Unit> {
        val userId = authentication.userId()
        val existing = fcmTokenRepository.findByToken(request.fcmToken)

        if (existing != null) {
            existing.userId = userId
            existing.updatedAt = Instant.now()
            logger.info("Reassigned FCM token to user {}", userId)
        } else {
            fcmTokenRepository.save(FcmTokenEntity(userId = userId, token = request.fcmToken))
            logger.info("Registered new FCM token for user {}", userId)
        }

        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/fcm-token")
    @Transactional
    fun unregisterToken(
        authentication: Authentication,
        @RequestBody(required = false) request: FcmTokenRequest?,
    ): ResponseEntity<Unit> {
        val userId = authentication.userId()

        val deleted = if (request?.fcmToken != null) {
            fcmTokenRepository.deleteByUserIdAndToken(userId, request.fcmToken)
        } else {
            // Older clients don't send a token on unregister; fall back to clearing every
            // device for this user rather than doing nothing.
            fcmTokenRepository.deleteAllByUserId(userId)
        }
        logger.info("Unregistered {} FCM token(s) for user {}", deleted, userId)

        return ResponseEntity.noContent().build()
    }

    @PostMapping("/test")
    @Transactional
    fun sendTestNotification(authentication: Authentication): ResponseEntity<Unit> {
        val tokens = fcmTokenRepository.findAllByUserId(authentication.userId())
        if (tokens.isEmpty()) return ResponseEntity.badRequest().build()

        tokens.forEach { fcmToken ->
            logger.info(
                "Sending test notification to user {} with FCM token {}",
                authentication.userId(),
                fcmToken.token.substring(0, 6) + "...",
            )

            val message = Message.builder()
                .setToken(fcmToken.token)
                .setNotification(
                    Notification.builder()
                        .setTitle("Test Notification")
                        .setBody("Push notifications are working!")
                        .build())
                .build()

            FirebaseMessaging.getInstance().send(message)
        }
        logger.info("Test notification sent successfully to user {}", authentication.userId())
        return ResponseEntity.noContent().build()
    }

    private fun Authentication.userId(): UUID = principal as UUID
}