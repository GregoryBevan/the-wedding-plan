package me.elgregos.theweddingplan.api.guest.request

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isEmpty
import assertk.assertions.isNotEmpty
import jakarta.validation.Validation
import jakarta.validation.Validator
import me.elgregos.theweddingplan.api.guest.request.AddGuestRequestFixtures.charlieDavis
import me.elgregos.theweddingplan.application.guest.command.AddGuestCommandFixtures.charlieDavis as charlieDavisCommand
import me.elgregos.theweddingplan.domain.guest.entity.Language
import kotlin.test.BeforeTest
import kotlin.test.Test

class AddGuestRequestTest {

    private lateinit var validator: Validator

    @BeforeTest
    fun setUp() {
        validator = Validation.buildDefaultValidatorFactory().validator
    }

    @Test
    fun `should have no validation errors for a valid request`() {
        val violations = validator.validate(charlieDavis)

        assertThat(violations).isEmpty()
    }

    @Test
    fun `should have a validation error when first name is blank`() {
        val request = charlieDavis.copy(firstName = "  ")

        val violations = validator.validate(request)

        assertThat(violations).isNotEmpty()
    }

    @Test
    fun `should have a validation error when email is invalid`() {
        val request = charlieDavis.copy(email = "not-an-email")

        val violations = validator.validate(request)

        assertThat(violations).isNotEmpty()
    }

    @Test
    fun `should map request to command`() {
        assertThat(charlieDavis.toCommand(Language.FR)).isEqualTo(charlieDavisCommand)
    }
}

