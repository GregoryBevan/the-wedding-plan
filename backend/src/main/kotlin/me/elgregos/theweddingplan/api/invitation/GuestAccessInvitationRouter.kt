package me.elgregos.theweddingplan.api.invitation

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.function.router

@Configuration
class GuestAccessInvitationRouter(private val guestAccessInvitationEndpoint: GuestAccessInvitationEndpoint) {

    @Bean
    fun guestAccessInvitationRoute() = router {
        "/guest-access/invitations".nest {
            GET("/{token}", guestAccessInvitationEndpoint::resolveByAccessToken)
        }
    }
}


