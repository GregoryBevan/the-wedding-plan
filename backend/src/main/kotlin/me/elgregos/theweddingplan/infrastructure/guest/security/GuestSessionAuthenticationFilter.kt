package me.elgregos.theweddingplan.infrastructure.guest.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import me.elgregos.theweddingplan.domain.guest.service.GuestSessionTokens
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

const val GUEST_SESSION_COOKIE = "guest_session"

@Component
class GuestSessionAuthenticationFilter(
    private val guestSessionTokens: GuestSessionTokens,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        if (SecurityContextHolder.getContext().authentication == null) {
            request.cookies
                ?.firstOrNull { it.name == GUEST_SESSION_COOKIE }
                ?.value
                ?.let(guestSessionTokens::verify)
                ?.let { guestSession ->
                    SecurityContextHolder.setContext(
                        SecurityContextHolder.createEmptyContext().apply {
                            authentication = GuestSessionAuthenticationToken.authenticated(guestSession)
                        }
                    )
                }
        }

        filterChain.doFilter(request, response)
    }
}

