package com.blindspot.blindspotapi.backend.auth

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Resolves the authenticated user from a `Authorization: Bearer <accessToken>` header. Requests
 * without a valid token simply proceed unauthenticated; [org.springframework.security.web.SecurityFilterChain]
 * is responsible for rejecting access to protected routes.
 */
@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
) : OncePerRequestFilter() {
    private val log = LoggerFactory.getLogger(this::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val header = request.getHeader("Authorization")

        if (header != null && header.startsWith("Bearer ")) {
            val token = header.removePrefix("Bearer ").trim()
            val userId = runCatching { jwtService.validateAccessToken(token) }.getOrNull()

            if (userId != null && SecurityContextHolder.getContext().authentication == null) {
                log.debug("Authenticated request for userId={} on {}", userId, request.requestURI)
                val authentication = UsernamePasswordAuthenticationToken(userId, null, emptyList())
                SecurityContextHolder.getContext().authentication = authentication
            } else {
                log.warn("Bearer token present but failed validation on {}", request.requestURI)
            }
        } else {
            log.debug("No Bearer token in Authorization header for {}", request.requestURI)
        }

        filterChain.doFilter(request, response)
    }
}
