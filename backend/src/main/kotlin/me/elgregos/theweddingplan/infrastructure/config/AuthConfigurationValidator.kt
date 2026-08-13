package me.elgregos.theweddingplan.infrastructure.config

import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
class AuthConfigurationValidator(
    environment: Environment,
    authProperties: AuthProperties,
) {

    init {
        if (environment.activeProfiles.contains("prod")) {
            check(authProperties.normalizedAdminEmails().isNotEmpty()) {
                "app.auth.admin-emails must not be empty in prod profile"
            }

            val redirectUrl = authProperties.successRedirectUrl.trim()
            check(redirectUrl.startsWith("https://") && !redirectUrl.contains("localhost")) {
                "app.auth.success-redirect-url must be a non-localhost https URL in prod profile"
            }
        }
    }
}