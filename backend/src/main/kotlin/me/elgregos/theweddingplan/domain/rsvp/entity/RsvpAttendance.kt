package me.elgregos.theweddingplan.domain.rsvp.entity

enum class RsvpAttendance {
    ATTENDING,
    DECLINED;

    companion object {
        fun parseOrNull(value: String?): RsvpAttendance? =
            value?.trim()?.uppercase()?.let { candidate -> entries.firstOrNull { it.name == candidate } }
    }
}


