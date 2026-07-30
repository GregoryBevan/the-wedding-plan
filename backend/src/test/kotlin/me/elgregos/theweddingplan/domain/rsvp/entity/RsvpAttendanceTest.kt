package me.elgregos.theweddingplan.domain.rsvp.entity

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test

class RsvpAttendanceTest {

    @Test
    fun `should parse a matching value`() {
        assertThat(RsvpAttendance.parseOrNull("ATTENDING")).isEqualTo(RsvpAttendance.ATTENDING)
    }

    @Test
    fun `should parse ignoring case and surrounding whitespace`() {
        assertThat(RsvpAttendance.parseOrNull("  declined  ")).isEqualTo(RsvpAttendance.DECLINED)
    }

    @Test
    fun `should parse to null for an unknown value`() {
        assertThat(RsvpAttendance.parseOrNull("MAYBE")).isNull()
    }

    @Test
    fun `should parse to null for a null value`() {
        assertThat(RsvpAttendance.parseOrNull(null)).isNull()
    }
}

