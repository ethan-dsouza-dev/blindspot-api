package com.blindspot.blindspotapi.backend.notifications

import com.blindspot.blindspotapi.backend.auth.repository.UserRepository
import com.blindspot.blindspotapi.backend.notifications.dto.FcmTokenRequest
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/notifications")
class NotificationsController(
    private val userRepository: UserRepository,
) {

    @PostMapping("/fcm-token")
    @Transactional
    fun registerToken(
        authentication: Authentication,
        @RequestBody request: FcmTokenRequest,
    ): ResponseEntity<Unit> {
        val user = userRepository.getReferenceById(authentication.userId())
        user.fcmToken = request.fcmToken
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/fcm-token")
    @Transactional
    fun unregisterToken(authentication: Authentication): ResponseEntity<Unit> {
        val user = userRepository.getReferenceById(authentication.userId())
        user.fcmToken = null
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/test")
    @Transactional
    fun sendTestNotification(authentication: Authentication): ResponseEntity<Unit> {
        val user = userRepository.getReferenceById(authentication.userId())
        val fcmToken = user.fcmToken ?: return ResponseEntity.badRequest().build()

        // Use Firebase Admin SDK to send a test push
        val message = Message.builder()
            .setToken(fcmToken)
            .setNotification(
                Notification.builder()
                .setTitle("Test Notification")
                .setBody("Push notifications are working!")
                .build())
            .build()

        FirebaseMessaging.getInstance().send(message)
        return ResponseEntity.noContent().build()
    }

    private fun Authentication.userId(): UUID = principal as UUID
}