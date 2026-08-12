package me.elgregos.theweddingplan.api.invitation.request

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isEmpty
import assertk.assertions.isNotEmpty
import assertk.assertions.isNull
import jakarta.validation.Validation
import jakarta.validation.Validator
import me.elgregos.theweddingplan.api.invitation.request.UpdateInvitationRequestFixtures.blankLabel
import me.elgregos.theweddingplan.api.invitation.request.UpdateInvitationRequestFixtures.malformedGuestId
import me.elgregos.theweddingplan.api.invitation.request.UpdateInvitationRequestFixtures.mixedGuestsWithWhitespace
import me.elgregos.theweddingplan.api.invitation.request.UpdateInvitationRequestFixtures.noGuest
import me.elgregos.theweddingplan.application.invitation.command.UpdateInvitationCommandFixtures.mixedGuests
import me.elgregos.theweddingplan.application.invitation.command.UpdateInvitationCommandFixtures.noGuest as noGuestCommand
import me.elgregos.theweddingplan.domain.invitation.entity.InvitationFixtures.brideFamilyInvitation
import kotlin.test.BeforeTest
import kotlin.test.Test

class UpdateInvitationRequestTest {

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
    fun `should have a validation error when version is negative`() {
        val request = UpdateInvitationRequest(
            version = -1,
            label = "Valid",
            description = "description",
            guestIds = emptyList(),
        )

        val violations = validator.validate(request)

        assertThat(violations).isNotEmpty()
    }

    @Test
    fun `should map request to command`() {
        val command = mixedGuestsWithWhitespace.toCommandOrNull(brideFamilyInvitation.id)

        assertThat(command).isEqualTo(mixedGuests)
    }

    @Test
    fun `should return null when a guest id is malformed`() {
        val command = malformedGuestId.toCommandOrNull(brideFamilyInvitation.id)

        assertThat(command).isNull()
    }

    @Test
    fun `should map request with no guest ids to empty set`() {
        val command = noGuest.toCommandOrNull(brideFamilyInvitation.id)

        assertThat(command).isEqualTo(noGuestCommand)
    }
}
