package me.elgregos.theweddingplan.api.auth

import me.elgregos.theweddingplan.infrastructure.config.AuthProperties
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.csrf.CsrfToken
import org.springframework.stereotype.Component
import org.springframework.web.servlet.function.ServerRequest
import org.springframework.web.servlet.function.ServerResponse

@Component
class AuthEndpoint(private val authProperties: AuthProperties) {

    fun me(request: ServerRequest): ServerResponse {
        request.attribute(CsrfToken::class.java.name)

        val authentication = request.principal().orElse(null) as? org.springframework.security.core.Authentication
        val oauth2User = authentication?.principal as? OAuth2User
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
