package me.elgregos.theweddingplan.infrastructure.song

import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.DeleteExchange
import org.springframework.web.service.annotation.PostExchange

/**
 * Declarative HTTP client for Deezer's playlist API (authenticated via a `manage_library` token).
 * Backed by the shared `deezerRestClient` (see `DeezerConfig`); non-2xx responses surface as
 * `RestClientException` for the adapter to translate.
 */
interface DeezerPlaylistApi {

    /** Adds a track to the playlist. Deezer replies with a bare `true`, or a 200 carrying an `error` node. */
    @PostExchange("/playlist/{playlistId}/tracks")
    fun addTrack(
        @PathVariable playlistId: String,
        @RequestParam("songs") songId: Long,
        @RequestParam("access_token") accessToken: String,
    ): String?

    /** Removes a track from the playlist. Deezer replies with a bare `true`, or a 200 carrying an `error` node. */
    @DeleteExchange("/playlist/{playlistId}/tracks")
    fun removeTrack(
        @PathVariable playlistId: String,
        @RequestParam("songs") songId: Long,
        @RequestParam("access_token") accessToken: String,
    ): String?
}


