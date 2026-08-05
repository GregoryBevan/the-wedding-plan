package me.elgregos.theweddingplan.infrastructure.song

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import me.elgregos.theweddingplan.domain.song.entity.SongSuggestion

/**
 * Slim projection of the Deezer `/search` payload. Unknown fields are ignored so the
 * mapping stays resilient to Deezer adding attributes we don't consume.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class DeezerSearchResponse(
    val data: List<DeezerTrack>? = null,
    val error: DeezerError? = null,
) {
    fun toSuggestions(): List<SongSuggestion> = data.orEmpty().map(DeezerTrack::toSuggestion)
}

/**
 * Deezer signals failures (rate limiting, quota, invalid query, ...) with an HTTP 200
 * carrying an `error` node instead of a non-2xx status, so it must be inspected explicitly.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class DeezerError(
    val type: String? = null,
    val message: String? = null,
    val code: Int? = null,
) {
    val errorCode: DeezerErrorCode get() = DeezerErrorCode.fromCode(code)

    fun describe() = "$errorCode (code=$code, message=$message)"
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class DeezerTrack(
    val id: Long,
    val title: String,
    val link: String,
    val preview: String? = null,
    val artist: DeezerArtist,
) {
    fun toSuggestion() = SongSuggestion(
        deezerId = id,
        title = title,
        artist = artist.name,
        link = link,
        preview = preview?.takeIf(String::isNotBlank),
    )
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class DeezerArtist(
    val name: String,
)

