package me.elgregos.theweddingplan.api.guest

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.function.router

@Configuration
class GuestAccessRouter(
    private val guestAccessMagicLinkEndpoint: GuestAccessMagicLinkEndpoint,
) {

    @Bean
    fun guestAccessRoute() = router {
        "/guest-access/magic-links".nest {
            GET("/{token}", guestAccessMagicLinkEndpoint::verifyMagicLink)
        }

        POST("/guest-access/invitations/{token}/guests/{guestId}/magic-link-requests", guestAccessMagicLinkEndpoint::requestMagicLink)
    }
}





