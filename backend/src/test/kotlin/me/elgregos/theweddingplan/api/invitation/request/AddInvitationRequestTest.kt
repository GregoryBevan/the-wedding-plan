package me.elgregos.theweddingplan.api.invitation.request

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isEmpty
import assertk.assertions.isNotEmpty
import assertk.assertions.isNull
import jakarta.validation.Validation
import jakarta.validation.Validator
import me.elgregos.theweddingplan.api.invitation.request.AddInvitationRequestFixtures.blankLabel
import me.elgregos.theweddingplan.api.invitation.request.AddInvitationRequestFixtures.malformedGuestId
import me.elgregos.theweddingplan.api.invitation.request.AddInvitationRequestFixtures.mixedGuestsWithWhitespace
import me.elgregos.theweddingplan.api.invitation.request.AddInvitationRequestFixtures.noGuest
import me.elgregos.theweddingplan.application.invitation.command.AddInvitationCommandFixtures.mixedGuests
import kotlin.test.BeforeTest
import me.elgregos.theweddingplan.application.invitation.command.AddInvitationCommandFixtures.noGuest as noGuestCommand
import kotlin.test.Test

class AddInvitationRequestTest {

    private lateinit var validator: Validator

    @BeforeTest
    fun setup() {
        validator = Validation.buildDefaultValidatorFactory().validator
    }

    @Test
    fun `should have no validation errors for a valid request`() {
        val violations = validator.validate(mixedGuestsWithWhitespace)

        assertThat(violations).isEmpty()
    }

    @Test
    fun `should have a validation error when label is blank`() {
        val violations = validator.validate(blankLabel)

        assertThat(violations).isNotEmpty()
    }

    @Test
    fun `should map request to command`() {
        val command = mixedGuestsWithWhitespace.toCommandOrNull()

        assertThat(command).isEqualTo(mixedGuests)
    }

    @Test
    fun `should return null when a guest id is malformed`() {
        val command = malformedGuestId.toCommandOrNull()

        assertThat(command).isNull()
    }

    @Test
    fun `should map request with no guest ids to empty set`() {
        val command = noGuest.toCommandOrNull()

        assertThat(command).isEqualTo(noGuestCommand)
    }
}
