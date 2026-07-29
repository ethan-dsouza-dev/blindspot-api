package com.blindspot.blindspotapi.backend.auth

import com.blindspot.blindspotapi.backend.auth.config.JwtProperties
import com.blindspot.blindspotapi.backend.auth.entity.RefreshTokenEntity
import com.blindspot.blindspotapi.backend.auth.entity.UserEntity
import com.blindspot.blindspotapi.backend.auth.repository.RefreshTokenRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import java.time.Instant

class RefreshTokenServiceTest {
    private val repository: RefreshTokenRepository = mock()
    private val jwtProperties = JwtProperties(maxActiveRefreshTokensPerUser = 2)
    private val service = RefreshTokenService(repository, jwtProperties)

    @Test
    fun `issue deletes oldest active tokens when user exceeds cap`() {
        val user = UserEntity(googleSub = "sub", email = "a@b.com")
        val now = Instant.now()
        val existing = (1..3).map { i ->
            RefreshTokenEntity(
                user = user,
                tokenHash = "hash$i",
                expiresAt = now.plusSeconds(3600),
                createdAt = now.minusSeconds((4 - i).toLong()),
            )
        }.sortedBy { it.createdAt }

        val saveCaptor = argumentCaptor<RefreshTokenEntity>()
        whenever(repository.save(saveCaptor.capture())).thenAnswer { saveCaptor.lastValue }

        whenever(repository.findActiveByUser(eq(user), any())).thenAnswer { existing + saveCaptor.lastValue }

        service.issue(user)

        val deletedCaptor = argumentCaptor<Iterable<RefreshTokenEntity>>()
        verify(repository).deleteAll(deletedCaptor.capture())
        assertEquals(listOf("hash1", "hash2"), deletedCaptor.firstValue.toList().map { it.tokenHash })
    }
}
