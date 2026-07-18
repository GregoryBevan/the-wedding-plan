package me.elgregos.theweddingplan.domain.guest.entity

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import kotlin.test.Test

class GuestIdTest {

    @Test
    fun `should parse valid guest id with fromStringOrNull`() {
        val value = "019f70eb-f060-7d9f-8dd8-f9caeca9d078"

        assertThat(GuestId.fromStringOrNull(value)).isEqualTo(GuestId.fromString(value))
    }

    @Test
    fun `should return null when guest id is invalid`() {
        assertThat(GuestId.fromStringOrNull("invalid-id")).isEqualTo(null)
    }

    @Test
    fun `should fail when parsing invalid guest id with fromString`() {
        assertFailure { GuestId.fromString("invalid-id") }
            .isInstanceOf(IllegalArgumentException::class)
    }
}

