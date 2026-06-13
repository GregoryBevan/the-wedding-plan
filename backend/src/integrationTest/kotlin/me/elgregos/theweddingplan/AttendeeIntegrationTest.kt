package me.elgregos.theweddingplan

import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEqualTo
import assertk.assertions.isIn
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate

class AttendeeIntegrationTest : AbstractIntegrationTest() {

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Test
    fun `should have loaded attendee data`() {
        val count = jdbcTemplate.queryForObject("select count(*) from attendee", Int::class.java)
        assertThat(count).isEqualTo(2)
        
        val firstNames = jdbcTemplate.queryForList("select first_name from attendee where last_name = 'Doe'", String::class.java)
        assertThat(firstNames).containsExactlyInAnyOrder("John", "Jane")
    }
}
