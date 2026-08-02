package me.elgregos.theweddingplan.infrastructure.song

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.messageContains
import me.elgregos.theweddingplan.domain.song.SongCatalogUnavailableException
import me.elgregos.theweddingplan.domain.song.entity.SongSuggestionFixtures.laVieEnRose
import me.elgregos.theweddingplan.infrastructure.song.DeezerSearchResponseFixtures.laVieEnRoseJson
import me.elgregos.theweddingplan.infrastructure.song.DeezerSearchResponseFixtures.quotaExceededErrorJson
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withServerError
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestClient
import kotlin.test.BeforeTest
import kotlin.test.Test

class DeezerSongCatalogIT {

    private lateinit var server: MockRestServiceServer
    private lateinit var deezerSongCatalog: DeezerSongCatalog

    @BeforeTest
    fun setUp() {
        val builder = RestClient.builder().baseUrl("https://api.deezer.com")
        server = MockRestServiceServer.bindTo(builder).build()
        deezerSongCatalog = DeezerSongCatalog(builder.build())
    }

    @Test
    fun `should map deezer search results to suggestions`() {
        server.expect(requestTo("https://api.deezer.com/search?q=piaf"))
            .andRespond(withSuccess(laVieEnRoseJson, MediaType.APPLICATION_JSON))

        assertThat(deezerSongCatalog.search("piaf")).isEqualTo(listOf(laVieEnRose))
    }

    @Test
    fun `should return an empty list when deezer returns no content`() {
        server.expect(requestTo("https://api.deezer.com/search?q=piaf"))
            .andRespond(withSuccess())

        assertThat(deezerSongCatalog.search("piaf")).isEmpty()
    }

    @Test
    fun `should raise an unavailable error when deezer responds with an error`() {
        server.expect(requestTo("https://api.deezer.com/search?q=piaf"))
            .andRespond(withServerError())

        assertFailure { deezerSongCatalog.search("piaf") }
            .isInstanceOf(SongCatalogUnavailableException::class)
    }

    @Test
    fun `should raise an unavailable error when deezer responds with an error body on a 200`() {
        server.expect(requestTo("https://api.deezer.com/search?q=piaf"))
            .andRespond(withSuccess(quotaExceededErrorJson, MediaType.APPLICATION_JSON))

        assertFailure { deezerSongCatalog.search("piaf") }
            .isInstanceOf(SongCatalogUnavailableException::class)
            .messageContains("QUOTA (code=4, message=Quota limit exceeded)")
    }
}

