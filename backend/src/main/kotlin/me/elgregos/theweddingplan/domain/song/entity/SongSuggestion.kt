package me.elgregos.theweddingplan.domain.song.entity

data class SongSuggestion(
    val deezerId: Long,
    val title: String,
    val artist: String,
    val link: String,
    val preview: String? = null,
)

