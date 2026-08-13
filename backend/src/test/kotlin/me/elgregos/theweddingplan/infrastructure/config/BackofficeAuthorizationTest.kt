package me.elgregos.theweddingplan.infrastructure.config

import assertk.assertThat
import assertk.assertions.containsOnly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import kotlin.test.BeforeTest
import kotlin.test.Test

class BackofficeAuthorizationTest {

    private lateinit var backofficeAuthorization: BackofficeAuthorization

    @BeforeTest
    fun setUp() {
        backofficeAuthorization = BackofficeAuthorization(
            AuthProperties(
                adminEmails = listOf("Admin@Example.com"),
                readOnlyEmails = listOf("  Viewer@Example.com  "),
            )
        )
    }

    @Test
    fun `should resolve admin role for an allowed email`() {
        assertThat(backofficeAuthorization.roleOf("admin@example.com")).isEqualTo(BackofficeRole.ADMIN)
    }

    @Test
    fun `should resolve read-only role for a read-only email regardless of case and whitespace`() {
        assertThat(backofficeAuthorization.roleOf("viewer@example.com")).isEqualTo(BackofficeRole.READ_ONLY)
    }

    @Test
    fun `should resolve no role for an unknown email`() {
        assertThat(backofficeAuthorization.roleOf("stranger@example.com")).isNull()
    }

    @Test
    fun `should resolve no role for a null email`() {
        assertThat(backofficeAuthorization.roleOf(null)).isNull()
    }

    @Test
    fun `should prefer admin when an email is in both allowlists`() {
        val authorization = BackofficeAuthorization(
            AuthProperties(
                adminEmails = listOf("both@example.com"),
                readOnlyEmails = listOf("both@example.com"),
            )
        )

        assertThat(authorization.roleOf("both@example.com")).isEqualTo(BackofficeRole.ADMIN)
    }

    @Test
    fun `should grant read and write capabilities to an admin`() {
        assertThat(backofficeAuthorization.capabilitiesOf("admin@example.com"))
            .containsOnly(BackofficeCapability.READ, BackofficeCapability.WRITE)
    }

    @Test
    fun `should grant only the read capability to a read-only email`() {
        assertThat(backofficeAuthorization.capabilitiesOf("viewer@example.com"))
            .containsOnly(BackofficeCapability.READ)
    }

    @Test
    fun `should not grant any capability to an unknown email`() {
        assertThat(backofficeAuthorization.capabilitiesOf("stranger@example.com")).isEmpty()
    }

    @Test
    fun `should confirm a read-only email may read but not write`() {
        assertThat(backofficeAuthorization.hasCapability("viewer@example.com", BackofficeCapability.READ)).isTrue()
    }

    @Test
    fun `should confirm a read-only email lacks the write capability`() {
        assertThat(backofficeAuthorization.hasCapability("viewer@example.com", BackofficeCapability.WRITE)).isFalse()
    }

    @Test
    fun `should confirm an admin may write`() {
        assertThat(backofficeAuthorization.hasCapability("admin@example.com", BackofficeCapability.WRITE)).isTrue()
    }

    @Test
    fun `should expose the authoritative capability ids`() {
        assertThat(BackofficeCapability.READ.id).isEqualTo("backoffice.read")
        assertThat(BackofficeCapability.WRITE.id).isEqualTo("backoffice.write")
    }
}






