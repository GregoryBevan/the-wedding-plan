package me.elgregos.theweddingplan.domain.rsvp.entity

enum class Meal {
    MEAT,
    FISH,
    VEGGIE;

    companion object {
        fun parseOrNull(value: String?): Meal? =
            value?.trim()?.uppercase()?.let { candidate -> entries.firstOrNull { it.name == candidate } }
    }
}
