package me.elgregos.theweddingplan.api.auth

import me.elgregos.theweddingplan.api.common.clientAddress
import me.elgregos.theweddingplan.infrastructure.config.BackofficeAuthorization
import me.elgregos.theweddingplan.infrastructure.config.BackofficeCapability
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Component
import org.springframework.web.servlet.function.ServerRequest
import org.springframework.web.servlet.function.ServerResponse

@Component
class AuthEndpoint(
    private val backofficeAuthorization: BackofficeAuthorization,
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
                authenticated = oauth2User != null,
                email = email,
                authorized = backofficeAuthorization.hasCapability(email, BackofficeCapability.READ),
                canWrite = backofficeAuthorization.hasCapability(email, BackofficeCapability.WRITE),
            )
        )
    }
}

data class AuthStatusResponse(
    val authenticated: Boolean,
    val email: String?,
    val authorized: Boolean,
    val canWrite: Boolean,
)
