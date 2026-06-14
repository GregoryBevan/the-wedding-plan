package me.elgregos.theweddingplan.infrastructure.guest

import assertk.assertThat
import assertk.assertions.containsAtLeast
import assertk.assertions.isEqualTo
import me.elgregos.theweddingplan.AbstractIntegrationTest
import me.elgregos.theweddingplan.domain.guest.GuestFixtures
import me.elgregos.theweddingplan.domain.guest.GuestFixtures.janeDoe
import me.elgregos.theweddingplan.domain.guest.GuestFixtures.johnDoe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.uuid.toJavaUuid

class GuestExposedRepositoryIT : AbstractIntegrationTest() {

    @Autowired
    private lateinit var guestsRepository: GuestsExposedRepository

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

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

    @Test
    fun `should update an existing guest`() {
        val guest = GuestFixtures.guest(
            firstName = "Alice",
            lastName = "Smith",
            email = "alice.smith@example.com"
        )
        guestsRepository.add(guest)
        
        val updatedGuest = guest.copy(firstName = "Bob", updateDate = LocalDateTime.now(ZoneOffset.UTC))
        guestsRepository.update(updatedGuest)
        
        val firstName = jdbcTemplate.queryForObject("select first_name from guest where id = ?", String::class.java, updatedGuest.id.value.toJavaUuid())
        assertThat(firstName).isEqualTo("Bob")
    }

    @Test
    fun `should list all guests`() {

        val guests = guestsRepository.list()

        assertThat(guests).containsAtLeast(johnDoe, janeDoe)
    }
}
