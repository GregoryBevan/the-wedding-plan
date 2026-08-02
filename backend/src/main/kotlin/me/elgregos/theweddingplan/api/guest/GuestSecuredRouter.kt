package me.elgregos.theweddingplan.api.guest

import me.elgregos.theweddingplan.api.rsvp.GuestRsvpEndpoint
import me.elgregos.theweddingplan.api.song.SongSearchEndpoint
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.function.router

@Configuration
class GuestSecuredRouter(
    private val guestRsvpEndpoint: GuestRsvpEndpoint,
    private val guestSessionEndpoint: GuestSessionEndpoint,
    private val songSearchEndpoint: SongSearchEndpoint,
) {

    @Bean
    fun guestSecuredRoute() = router {
        GET("/api/guest-access/secured/me", guestSessionEndpoint::me)
        POST("/api/guest-access/secured/rsvp", guestRsvpEndpoint::submit)
        GET("/api/guest-access/secured/rsvp", guestRsvpEndpoint::fetch)
        GET("/api/guest-access/secured/song-search", songSearchEndpoint::search)
    }
}



