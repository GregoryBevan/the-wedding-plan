package me.elgregos.theweddingplan.domain.rsvp.entity

data class SongChoice(
    val deezerId: Long,
    val title: String,
    val artist: String,
    val link: String,
    val preview: String? = null,
)

