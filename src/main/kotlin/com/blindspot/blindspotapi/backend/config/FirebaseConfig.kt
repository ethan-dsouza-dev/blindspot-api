package com.blindspot.blindspotapi.backend.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.FileInputStream

/**
 * Initializes the Firebase Admin SDK once at startup using a service account key file. The
 * path comes from `FIREBASE_CREDENTIALS_PATH`, expected to point at a JSON key downloaded from
 * Firebase Console > Project settings > Service accounts — set as a Render environment
 * variable (mounted as a secret file), never committed to the repo.
 */
@Component
class FirebaseConfig(
    @Value("\${firebase.credentials-path}") private val credentialsPath: String,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @PostConstruct
    fun init() {
        if (FirebaseApp.getApps().isEmpty()) {
            val credentials = GoogleCredentials.fromStream(FileInputStream(credentialsPath))
            val options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .build()
            FirebaseApp.initializeApp(options)
            logger.info("Firebase Admin SDK initialized")
        }
    }
}