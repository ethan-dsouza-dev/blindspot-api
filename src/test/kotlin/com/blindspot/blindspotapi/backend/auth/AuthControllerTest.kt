package com.blindspot.blindspotapi.backend.auth

import org.junit.jupiter.api.Test
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@WebMvcTest(AuthController::class)
class AuthControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var googleTokenVerifier: GoogleTokenVerifier

    @MockitoBean
    lateinit var userService: UserService

    @MockitoBean
    lateinit var jwtService: JwtService

    @MockitoBean
    lateinit var refreshTokenService: RefreshTokenService

    @Test
    fun `sign out returns 204 and revokes refresh token`() {
        mockMvc.post("/api/auth/sign-out") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"refreshToken":"some-token"}"""
        }.andExpect {
            status { isNoContent() }
        }

        verify(refreshTokenService).revoke(eq("some-token"))
    }
}
