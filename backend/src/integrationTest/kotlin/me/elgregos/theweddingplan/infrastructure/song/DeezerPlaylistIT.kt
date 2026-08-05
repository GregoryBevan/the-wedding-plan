package me.elgregos.theweddingplan.infrastructure.song

import assertk.assertFailure
import assertk.assertions.isInstanceOf
import assertk.assertions.messageContains
import me.elgregos.theweddingplan.domain.song.WeddingPlaylistUnavailableException
import me.elgregos.theweddingplan.infrastructure.config.DeezerProperties
import me.elgregos.theweddingplan.infrastructure.song.DeezerPlaylistFixtures.addTrackSuccessJson
import me.elgregos.theweddingplan.infrastructure.song.DeezerPlaylistFixtures.invalidTokenErrorJson
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withServerError
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestClient
import org.springframework.web.client.support.RestClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import kotlin.test.BeforeTest
import kotlin.test.Test

class DeezerPlaylistIT {

    private lateinit var server: MockRestServiceServer
    private lateinit var deezerPlaylist: DeezerPlaylist

    @BeforeTest
    fun setUp() {
        val builder = RestClient.builder().baseUrl("https://api.deezer.com")
        server = MockRestServiceServer.bindTo(builder).build()
        val deezerPlaylistApi = HttpServiceProxyFactory
            .builderFor(RestClientAdapter.create(builder.build()))
            .build()
            .createClient(DeezerPlaylistApi::class.java)
        deezerPlaylist = DeezerPlaylist(
            deezerPlaylistApi,
            DeezerProperties(accessToken = "tok", playlistId = "42"),
        )
    }

    @Test
    fun `should add a track to the playlist`() {
        server.expect(requestTo("https://api.deezer.com/playlist/42/tracks?songs=3135556&access_token=tok"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess(addTrackSuccessJson, MediaType.APPLICATION_JSON))

        deezerPlaylist.addTrack(3135556L)

        server.verify()
    }

    @Test
    fun `should raise an unavailable error when the add fails`() {
        server.expect(requestTo("https://api.deezer.com/playlist/42/tracks?songs=3135556&access_token=tok"))
            .andRespond(withServerError())

        assertFailure { deezerPlaylist.addTrack(3135556L) }
            .isInstanceOf(WeddingPlaylistUnavailableException::class)
    }

    @Test
    fun `should raise an unavailable error when the add responds with an error body`() {
        server.expect(requestTo("https://api.deezer.com/playlist/42/tracks?songs=3135556&access_token=tok"))
            .andRespond(withSuccess(invalidTokenErrorJson, MediaType.APPLICATION_JSON))

        assertFailure { deezerPlaylist.addTrack(3135556L) }
            .isInstanceOf(WeddingPlaylistUnavailableException::class)
            .messageContains("Invalid OAuth access token")
    }

    @Test
    fun `should remove a track from the playlist`() {
        server.expect(requestTo("https://api.deezer.com/playlist/42/tracks?songs=3135556&access_token=tok"))
            .andExpect(method(HttpMethod.DELETE))
            .andRespond(withSuccess(addTrackSuccessJson, MediaType.APPLICATION_JSON))

        deezerPlaylist.removeTrack(3135556L)

        server.verify()
    }

    @Test
    fun `should raise an unavailable error when the removal fails`() {
        server.expect(requestTo("https://api.deezer.com/playlist/42/tracks?songs=3135556&access_token=tok"))
            .andRespond(withServerError())

        assertFailure { deezerPlaylist.removeTrack(3135556L) }
            .isInstanceOf(WeddingPlaylistUnavailableException::class)
    }

    @Test
    fun `should raise an unavailable error when the removal responds with an error body`() {
        server.expect(requestTo("https://api.deezer.com/playlist/42/tracks?songs=3135556&access_token=tok"))
            .andRespond(withSuccess(invalidTokenErrorJson, MediaType.APPLICATION_JSON))

        assertFailure { deezerPlaylist.removeTrack(3135556L) }
            .isInstanceOf(WeddingPlaylistUnavailableException::class)
            .messageContains("Invalid OAuth access token")
    }
}




