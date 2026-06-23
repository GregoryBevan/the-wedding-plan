package me.elgregos.theweddingplan.api.guest

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.function.router

@Configuration
class GuestRouter(private val guestEndpoint: GuestEndpoint) {

    @Bean
    fun guestRoute() = router {
        "/api/guests".nest {
            GET("", guestEndpoint::listGuests)
            POST("", guestEndpoint::addGuest)
        }
    }
}
