package me.elgregos.theweddingplan.domain.rsvp.entity

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import kotlin.test.Test

class GuestRsvpIdTest {

    @Test
    fun `should round-trip a valid uuid through fromString and toString`() {
        val value = "019fb445-4209-75ad-9370-e16ff6140b37"

        assertThat(GuestRsvpId.fromString(value).toString()).isEqualTo(value)
    }

    @Test
    fun `should fail when parsing an invalid uuid with fromString`() {
        assertFailure { GuestRsvpId.fromString("invalid-id") }
            .isInstanceOf(IllegalArgumentException::class)
    }
}

