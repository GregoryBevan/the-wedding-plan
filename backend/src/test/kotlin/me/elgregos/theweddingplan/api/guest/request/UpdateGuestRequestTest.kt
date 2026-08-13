package me.elgregos.theweddingplan.api.guest.request

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isEmpty
import assertk.assertions.isNotEmpty
import jakarta.validation.Validation
import jakarta.validation.Validator
import me.elgregos.theweddingplan.api.guest.request.UpdateGuestRequestFixtures.johnDoeUpdated
import me.elgregos.theweddingplan.application.guest.command.UpdateGuestCommandFixtures.johnDoeUpdated as johnDoeUpdatedCommand
import me.elgregos.theweddingplan.domain.guest.entity.GuestFixtures.johnDoe
import me.elgregos.theweddingplan.domain.guest.entity.Language
import kotlin.test.BeforeTest
import kotlin.test.Test

class UpdateGuestRequestTest {

    private lateinit var validator: Validator

    @BeforeTest
    fun setup() {
        validator = Validation.buildDefaultValidatorFactory().validator
    }

    @Test
    fun `should have no validation errors for a valid request`() {
        val violations = validator.validate(johnDoeUpdated)

        assertThat(violations).isEmpty()
    }

    @Test
    fun `should have a validation error when version is negative`() {
        val request = johnDoeUpdated.copy(version = -1)

        val violations = validator.validate(request)

        assertThat(violations).isNotEmpty()
    }

    @Test
    fun `should have a validation error when email is invalid`() {
        val request = johnDoeUpdated.copy(email = "invalid")

        val violations = validator.validate(request)

        assertThat(violations).isNotEmpty()
    }

    @Test
    fun `should preserve language when omitted by mapping it to null`() {
        val command = johnDoeUpdated.toCommand(johnDoe.id)

        assertThat(command).isEqualTo(johnDoeUpdatedCommand)
    }

    @Test
    fun `should map a provided language to the command`() {
        val command = johnDoeUpdated.copy(language = "EN").toCommand(johnDoe.id)

        assertThat(command.language).isEqualTo(Language.EN)
    }
}