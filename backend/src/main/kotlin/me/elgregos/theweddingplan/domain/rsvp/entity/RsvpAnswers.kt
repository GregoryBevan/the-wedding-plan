package me.elgregos.theweddingplan.domain.rsvp.entity

/**
 * Structured answers a guest gives once attending. The meal is mandatory (it is a
 * non-nullable field, so answers cannot exist without one) while the song is optional.
 */
data class RsvpAnswers(
    val meal: Meal,
    val song: SongChoice? = null,
) {
    companion object
}

