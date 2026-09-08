package com.blindspot.blindspotapi.backend.notifications

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class NotificationDispatchService {
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun sendReminder(fcmToken: String, title: String, body: String) {
        val message = Message.builder()
            .setToken(fcmToken)
            .setNotification(
                Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build(),
            )
            .build()

        runCatching {
            FirebaseMessaging.getInstance().send(message)
        }.onFailure { e ->
            logger.warn("Failed to send FCM notification: {}", e.message)
        }
    }
}