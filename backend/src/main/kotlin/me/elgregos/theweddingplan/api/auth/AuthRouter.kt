package me.elgregos.theweddingplan.api.auth

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.function.router

@Configuration
class AuthRouter(private val authEndpoint: AuthEndpoint) {

    @Bean
    fun authRoute() = router {
        "/auth".nest {
            GET("/me", authEndpoint::me)
        }
    }
}
