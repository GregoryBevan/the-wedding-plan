package me.elgregos.theweddingplan.domain.invitation.entity

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import kotlin.test.Test

class InvitationIdTest {

    @Test
    fun `should parse valid invitation id with fromStringOrNull`() {
        val value = "019f2282-7971-77e6-8d25-7568739fca0f"

        assertThat(InvitationId.fromStringOrNull(value)).isEqualTo(InvitationId.fromString(value))
    }

    @Test
    fun `should return null when invitation id is invalid`() {
        assertThat(InvitationId.fromStringOrNull("invalid-id")).isEqualTo(null)
    }

    @Test
    fun `should fail when parsing invalid invitation id with fromString`() {
        assertFailure { InvitationId.fromString("invalid-id") }
            .isInstanceOf(IllegalArgumentException::class)
    }
}

