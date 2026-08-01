package me.elgregos.theweddingplan.domain.guest.entity

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import java.util.Locale
import kotlin.test.Test

class LanguageTest {

    @Test
    fun `should parse a matching value`() {
        assertThat(Language.parseOrNull("EN")).isEqualTo(Language.EN)
    }

    @Test
    fun `should parse ignoring case and surrounding whitespace`() {
        assertThat(Language.parseOrNull("  fr  ")).isEqualTo(Language.FR)
    }

    @Test
    fun `should parse to null for an unknown value`() {
        assertThat(Language.parseOrNull("ES")).isNull()
    }

    @Test
    fun `should parse to null for a null value`() {
        assertThat(Language.parseOrNull(null)).isNull()
    }

    @Test
    fun `should fall back to the default when the value is unknown`() {
        assertThat(Language.fromNullable("ES", Language.EN)).isEqualTo(Language.EN)
    }

    @Test
    fun `should keep the parsed value over the default`() {
        assertThat(Language.fromNullable("FR", Language.EN)).isEqualTo(Language.FR)
    }

    @Test
    fun `should map FR to the French locale`() {
        assertThat(Language.FR.toLocale()).isEqualTo(Locale.FRENCH)
    }

    @Test
    fun `should map EN to the English locale`() {
        assertThat(Language.EN.toLocale()).isEqualTo(Locale.ENGLISH)
    }
}

