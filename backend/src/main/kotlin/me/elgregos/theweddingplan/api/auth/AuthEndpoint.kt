package me.elgregos.theweddingplan.api.auth

import me.elgregos.theweddingplan.api.common.clientAddress
import me.elgregos.theweddingplan.infrastructure.config.AuthProperties
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Component
import org.springframework.web.servlet.function.ServerRequest
import org.springframework.web.servlet.function.ServerResponse

@Component
class AuthEndpoint(
    private val authProperties: AuthProperties,
    private val authRateLimiter: AuthRateLimiter,
) {

    fun me(request: ServerRequest): ServerResponse {
        val decision = authRateLimiter.check(request.clientAddress())

        if (!decision.allowed) {
            return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", decision.retryAfterSeconds.toString())
                .build()
        }

        val authentication = request.principal().orElse(null) as? org.springframework.security.core.Authentication
        val oauth2User = authentication?.principal as? OAuth2User
        val email = oauth2User?.getAttribute<String>("email")

        return ServerResponse.ok().body(
            AuthStatusResponse(
                isAuthenticated = oauth2User != null,
                email = email,
                // Backoffice entry is admin-only for now, matching the still-admin-only API gate
                // (SecurityConfig). This broadens to "has any backoffice role" — and starts reporting
                // the resolved capabilities — once read-only reads are enforced (#179) and the UI
                // consumes them (#180); flipping it earlier would admit read-only users into a UI whose
                // API calls all return 403.
                isAuthorized = authProperties.isAdmin(email)
            )
        )
    }
}

data class AuthStatusResponse(
    val isAuthenticated: Boolean,
    val email: String?,
    val isAuthorized: Boolean,
)
