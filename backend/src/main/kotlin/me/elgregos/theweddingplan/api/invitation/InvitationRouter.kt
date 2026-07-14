package me.elgregos.theweddingplan.api.invitation

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.function.router

@Configuration
class InvitationRouter(private val invitationEndpoint: InvitationEndpoint) {

    @Bean
    fun invitationRoute() = router {
        "/api/invitations".nest {
            GET("", invitationEndpoint::listInvitations)
            POST("", invitationEndpoint::addInvitation)
            GET("/{id}", invitationEndpoint::getInvitation)
            PUT("/{id}", invitationEndpoint::updateInvitation)
        }
    }
}

