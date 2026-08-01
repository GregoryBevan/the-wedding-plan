package me.elgregos.theweddingplan.domain.rsvp.entity

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test

class MealTest {

    @Test
    fun `should parse a matching value`() {
        assertThat(Meal.parseOrNull("MEAT")).isEqualTo(Meal.MEAT)
    }

    @Test
    fun `should parse ignoring case and surrounding whitespace`() {
        assertThat(Meal.parseOrNull("  veggie  ")).isEqualTo(Meal.VEGGIE)
    }

    @Test
    fun `should parse to null for an unknown value`() {
        assertThat(Meal.parseOrNull("PIZZA")).isNull()
    }

    @Test
    fun `should parse to null for a null value`() {
        assertThat(Meal.parseOrNull(null)).isNull()
    }
}

