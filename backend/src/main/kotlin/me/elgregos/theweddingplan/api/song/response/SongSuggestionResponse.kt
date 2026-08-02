package me.elgregos.theweddingplan.api.song.response

import me.elgregos.theweddingplan.domain.song.entity.SongSuggestion

data class SongSuggestionResponse(
    val deezerId: Long,
    val title: String,
    val artist: String,
    val link: String,
    val preview: String?,
)

internal fun SongSuggestion.toResponse() = SongSuggestionResponse(
    deezerId = deezerId,
    title = title,
    artist = artist,
    link = link,
    preview = preview,
)

