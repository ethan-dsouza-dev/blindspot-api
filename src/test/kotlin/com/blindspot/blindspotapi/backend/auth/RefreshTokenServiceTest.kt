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

    @Test
    fun `revoke marks active token as revoked`() {
        val user = UserEntity(googleSub = "sub", email = "a@b.com")
        val now = Instant.now()
        val entity = RefreshTokenEntity(
            user = user,
            tokenHash = "hash",
            expiresAt = now.plusSeconds(3600),
        )

        whenever(repository.findByTokenHash(any())).thenReturn(entity)
        whenever(repository.save(entity)).thenAnswer { entity }

        service.revoke("raw")

        assert(entity.revokedAt != null)
        verify(repository).save(entity)
    }

    @Test
    fun `revoke silently ignores unknown token`() {
        whenever(repository.findByTokenHash(any())).thenReturn(null)

        service.revoke("raw")

        verify(repository, never()).save(any())
    }

    @Test
    fun `revoke silently ignores already revoked token`() {
        val user = UserEntity(googleSub = "sub", email = "a@b.com")
        val entity = RefreshTokenEntity(
            user = user,
            tokenHash = "hash",
            expiresAt = Instant.now().plusSeconds(3600),
            revokedAt = Instant.now().minusSeconds(1),
        )

        whenever(repository.findByTokenHash(any())).thenReturn(entity)

        service.revoke("raw")

        verify(repository, never()).save(any())
    }
}
