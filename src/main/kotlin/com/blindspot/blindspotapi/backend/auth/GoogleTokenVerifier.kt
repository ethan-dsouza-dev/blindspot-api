package com.blindspot.blindspotapi.backend.auth

import com.blindspot.blindspotapi.backend.auth.config.GoogleAuthProperties
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import java.io.IOException
import java.security.GeneralSecurityException

/**
 * Verified identity extracted from a Google-issued ID token.
 */
data class GoogleIdentity(
    val sub: String,
    val email: String,
    val name: String?,
    val pictureUrl: String?,
)

/**
 * Verifies Google ID tokens (signature, audience, expiry) against Google's public certs.
 * Requires [GoogleAuthProperties.webClientId] to be configured; without it every token is
 * rejected rather than accepted with a weakened check.
 */
@Component
class GoogleTokenVerifier(
    private val googleAuthProperties: GoogleAuthProperties,
) {
    private val verifier: GoogleIdTokenVerifier? by lazy {
        val webClientId = googleAuthProperties.webClientId
        if (webClientId.isBlank()) {
            null
        } else {
            GoogleIdTokenVerifier.Builder(NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(listOf(webClientId))
                .build()
        }
    }

    fun verify(idTokenString: String): GoogleIdentity {
        val verifier = this.verifier
            ?: throw ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Google sign-in is not configured (missing GOOGLE_OAUTH_WEB_CLIENT_ID)",
            )

        val verifiedIdToken: GoogleIdToken? = try {
            verifier.verify(idTokenString)
        } catch (e: IOException) {
            throw ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Google public key endpoint is unreachable",
                e,
            )
        } catch (e: GeneralSecurityException) {
            throw ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Google ID token could not be verified",
                e,
            )
        }

        val idToken = verifiedIdToken
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Google ID token")

        val payload = idToken.payload
        val email = payload.email
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Google ID token missing email")

        return GoogleIdentity(
            sub = payload.subject,
            email = email,
            name = payload["name"] as? String,
            pictureUrl = payload["picture"] as? String,
        )
    }
}
