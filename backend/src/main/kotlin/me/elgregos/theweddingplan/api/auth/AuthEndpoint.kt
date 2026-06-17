package me.elgregos.theweddingplan.api.auth

import me.elgregos.theweddingplan.infrastructure.config.AuthProperties
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Component
import org.springframework.web.servlet.function.ServerRequest
import org.springframework.web.servlet.function.ServerResponse

@Component
class AuthEndpoint(private val authProperties: AuthProperties) {

    fun me(request: ServerRequest): ServerResponse {
        val principal = request.principal().orElse(null)
        val oauth2User = principal as? OAuth2User
        val email = oauth2User?.getAttribute<String>("email")

        return ServerResponse.ok().body(
            AuthStatusResponse(
                isAuthenticated = oauth2User != null,
                email = email,
                isAuthorized = authProperties.isAllowed(email)
            )
        )
    }
}

data class AuthStatusResponse(
    val isAuthenticated: Boolean,
    val email: String?,
    val isAuthorized: Boolean,
)
