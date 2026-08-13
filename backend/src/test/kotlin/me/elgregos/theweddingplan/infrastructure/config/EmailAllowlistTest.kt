package me.elgregos.theweddingplan.infrastructure.config

import assertk.assertThat
import assertk.assertions.containsOnly
import assertk.assertions.isEmpty
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import kotlin.test.Test

class EmailAllowlistTest {

    private val allowlist = EmailAllowlist(listOf("  Admin@Example.com  ", "viewer@example.com", "   "))

    @Test
    fun `should normalize entries by trimming, lowercasing, and dropping blanks`() {
        assertThat(allowlist.values).containsOnly("admin@example.com", "viewer@example.com")
    }

    @Test
    fun `should match an email regardless of case and surrounding whitespace`() {
        assertThat("  ADMIN@example.COM  " in allowlist).isTrue()
    }

    @Test
    fun `should not match an unknown email`() {
        assertThat("stranger@example.com" in allowlist).isFalse()
    }

    @Test
    fun `should not match a null email`() {
        assertThat(null in allowlist).isFalse()
    }

    @Test
    fun `should not match a blank email`() {
        assertThat("   " in allowlist).isFalse()
    }

    @Test
    fun `should expose no values for an empty allowlist`() {
        assertThat(EmailAllowlist(emptyList()).values).isEmpty()
    }
}

