package me.elgregos.theweddingplan.infrastructure.guest

import assertk.assertThat
import assertk.assertions.isEqualTo
import me.elgregos.theweddingplan.AbstractIntegrationTest
import me.elgregos.theweddingplan.domain.guest.GuestFixtures
import me.elgregos.theweddingplan.infrastructure.guest.GuestsExposedRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import kotlin.test.Test

class GuestRepositoryIT : AbstractIntegrationTest() {

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate
    
    @Autowired
    private lateinit var guestsRepository: GuestsExposedRepository

    @Test
    fun `should insert a new guest`() {
        val initialCount = jdbcTemplate.queryForObject("select count(*) from guest", Int::class.java) ?: 0
        val guest = GuestFixtures.guest(
            firstName = "Alice",
            lastName = "Smith",
            email = "alice.smith@example.com"
        )
        
        guestsRepository.add(guest)
        
        val count = jdbcTemplate.queryForObject("select count(*) from guest", Int::class.java) ?: 0
        assertThat(count).isEqualTo(initialCount + 1)
    }
}
