package me.elgregos.theweddingplan.api.guest

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.function.router

@Configuration
class GuestSecuredRouter(
    private val guestRsvpEndpoint: GuestRsvpEndpoint,
) {

    @Bean
    fun guestSecuredRoute() = router {
        POST("/api/guest-access/secured/rsvp", guestRsvpEndpoint::submit)
    }
}



