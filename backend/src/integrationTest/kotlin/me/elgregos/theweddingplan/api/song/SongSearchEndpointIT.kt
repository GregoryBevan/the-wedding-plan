package me.elgregos.theweddingplan.api.song

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import me.elgregos.theweddingplan.AbstractEndpointIntegrationTest
import me.elgregos.theweddingplan.api.song.SongSearchEndpointIT.StubSongCatalogConfig
import me.elgregos.theweddingplan.api.song.response.SongSuggestionResponse
import me.elgregos.theweddingplan.api.song.response.SongSuggestionResponseFixtures.laVieEnRose
import me.elgregos.theweddingplan.domain.guest.entity.GuestFixtures.janeDoe
import me.elgregos.theweddingplan.domain.guest.entity.GuestFixtures.johnDoe
import me.elgregos.theweddingplan.domain.song.SongCatalog
import me.elgregos.theweddingplan.domain.song.SongCatalogUnavailableException
import me.elgregos.theweddingplan.domain.song.entity.SongSuggestion
import me.elgregos.theweddingplan.domain.song.entity.SongSuggestionFixtures
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import kotlin.test.Test

@Import(StubSongCatalogConfig::class)
class SongSearchEndpointIT : AbstractEndpointIntegrationTest() {


    @Test
    fun `should reject song search without guest session`() {
        restTestClient.get().uri("/api/guest-access/secured/song-search?q=piaf")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun `should reject song search when guest does not belong to invitation`() {
        restTestClient.get().uri("/api/guest-access/secured/song-search?q=piaf")
            .header(HttpHeaders.COOKIE, guestSessionCookie(johnDoe.id))
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isForbidden
    }

    @Test
    fun `should return mapped suggestions for an authenticated guest`() {
        val suggestions = restTestClient.get().uri("/api/guest-access/secured/song-search?q=piaf")
            .header(HttpHeaders.COOKIE, guestSessionCookie(janeDoe.id))
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody(Array<SongSuggestionResponse>::class.java)
            .returnResult()
            .responseBody
            ?: error("Expected suggestions in response body")

        assertThat(suggestions.toList()).containsExactly(laVieEnRose)
    }

    @Test
    fun `should return an empty list for a blank query`() {
        val suggestions = restTestClient.get().uri("/api/guest-access/secured/song-search?q={q}", "  ")
            .header(HttpHeaders.COOKIE, guestSessionCookie(janeDoe.id))
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody(Array<SongSuggestionResponse>::class.java)
            .returnResult()
            .responseBody
            ?: error("Expected suggestions in response body")

        assertThat(suggestions.toList()).isEmpty()
    }

    @Test
    fun `should return a bad gateway when the catalog is unavailable`() {
        restTestClient.get().uri("/api/guest-access/secured/song-search?q=boom")
            .header(HttpHeaders.COOKIE, guestSessionCookie(janeDoe.id))
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(502)
    }


    @TestConfiguration
    class StubSongCatalogConfig {

        // Deterministic catalog so the endpoint/security wiring is exercised without a real Deezer call.
        @Bean
        @Primary
        fun stubSongCatalog(): SongCatalog = object : SongCatalog {
            override fun search(query: String): List<SongSuggestion> {
                if (query == "boom") throw SongCatalogUnavailableException(RuntimeException("upstream down"))
                return listOf(SongSuggestionFixtures.laVieEnRose)
            }
        }
    }
}


