package me.elgregos.theweddingplan.infrastructure.song

import me.elgregos.theweddingplan.domain.song.WeddingPlaylist
import me.elgregos.theweddingplan.domain.song.WeddingPlaylistUnavailableException
import me.elgregos.theweddingplan.infrastructure.config.DeezerProperties
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientException

/**
 * Deezer-backed [WeddingPlaylist]. Appends or removes a track on the configured playlist through the
 * authenticated `manage_library` token. De-duplication (add) and orphan detection (remove) are
 * handled by the caller.
 *
 * Every failure mode — a non-2xx status, a 200 with an `error` node, or an I/O/deserialization
 * problem — is translated into [WeddingPlaylistUnavailableException] so the caller stays isolated.
 */
@Component
class DeezerPlaylist(
    private val deezerPlaylistApi: DeezerPlaylistApi,
    private val properties: DeezerProperties,
) : WeddingPlaylist {

    override fun addTrack(deezerId: Long) {
        val body = call { deezerPlaylistApi.addTrack(properties.playlistId, deezerId, properties.accessToken) }
        verifyAccepted("adding", deezerId, body)
    }

    override fun removeTrack(deezerId: Long) {
        val body = call { deezerPlaylistApi.removeTrack(properties.playlistId, deezerId, properties.accessToken) }
        verifyAccepted("removing", deezerId, body)
    }

    private inline fun call(request: () -> String?): String =
        try {
            request().orEmpty().trim()
        } catch (e: RestClientException) {
            throw WeddingPlaylistUnavailableException(e)
        }

    // Deezer replies with a bare `true` on success, or a 200 carrying an `error` node.
    private fun verifyAccepted(action: String, deezerId: Long, body: String) {
        if (!body.equals("true", ignoreCase = true)) {
            throw WeddingPlaylistUnavailableException("$action track $deezerId was rejected: $body")
        }
    }
}





