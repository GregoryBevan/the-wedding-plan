package me.elgregos.theweddingplan.infrastructure.guest

import assertk.assertThat
import assertk.assertions.containsAtLeast
import assertk.assertions.isEqualTo
import me.elgregos.theweddingplan.AbstractIntegrationTest
import me.elgregos.theweddingplan.domain.shared.Dates
import me.elgregos.theweddingplan.domain.guest.Guest
import me.elgregos.theweddingplan.domain.guest.GuestFixtures
import me.elgregos.theweddingplan.domain.guest.GuestFixtures.janeDoe
import me.elgregos.theweddingplan.domain.guest.GuestFixtures.johnDoe
import me.elgregos.theweddingplan.domain.guest.GuestPage
import me.elgregos.theweddingplan.domain.guest.GuestId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import kotlin.test.Test
import kotlin.uuid.toJavaUuid

class GuestExposedRepositoryIT : AbstractIntegrationTest() {

    @Autowired
    private lateinit var guestsRepository: GuestsExposedRepository

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Test
    fun `should insert a new guest`() {
        val initialCount = guestCount()
        val guest = GuestFixtures.emmaWilson

        guestsRepository.add(guest)
        
        val count = guestCount()
        val persistedGuest = guestById(guest.id)

        assertThat(count).isEqualTo(initialCount + 1)
        assertThat(persistedGuest).isEqualTo(guest)
    }

    @Test
    fun `should update an existing guest`() {
        val guest = GuestFixtures.liamMiller
        guestsRepository.add(guest)
        
        val updatedGuest = guest.copy(
            version = 2L,
            firstName = "Noah",
            lastName = "Anderson",
            email = "noah.anderson@example.com",
            updateDate = Dates.nowUtcMillis()
        )

        guestsRepository.update(updatedGuest)

        val persistedGuest = guestById(updatedGuest.id)

        assertThat(persistedGuest).isEqualTo(updatedGuest)
    }

    @Test
    fun `should list all guests`() {

        val guests = guestsRepository.list()

        assertThat(guests).containsAtLeast(johnDoe, janeDoe)
    }

    @Test
    fun `should list guests with pagination`() {
        val totalGuests = guestCount()

        val firstPage = guestsRepository.list(page = 0, size = 1)
        val secondPage = guestsRepository.list(page = 1, size = 1)

        assertThat(firstPage.items).isEqualTo(listOf(johnDoe))
        assertPageMetadata(page = 0, size = 1, totalGuests = totalGuests, result = firstPage)

        assertThat(secondPage.items).isEqualTo(listOf(janeDoe))
        assertPageMetadata(page = 1, size = 1, totalGuests = totalGuests, result = secondPage)
    }

    private fun guestCount() = jdbcTemplate.queryForObject("select count(*) from guest", Int::class.java) ?: 0

    private fun guestById(guestId: GuestId): Guest =
        jdbcTemplate.queryForObject(
            """
            select id, version, creation_date, update_date, first_name, last_name, email
            from guest
            where id = ?
            """.trimIndent(),
            { rs, _ ->
                Guest(
                    id = GuestId.fromString(rs.getObject("id", java.util.UUID::class.java).toString()),
                    version = rs.getLong("version"),
                    creationDate = rs.getTimestamp("creation_date").toLocalDateTime(),
                    updateDate = rs.getTimestamp("update_date").toLocalDateTime(),
                    firstName = rs.getString("first_name"),
                    lastName = rs.getString("last_name"),
                    email = rs.getString("email")
                )
            },
            guestId.value.toJavaUuid()
        )

    private fun assertPageMetadata(page: Int, size: Int, totalGuests: Int, result: GuestPage) {
        assertThat(result.page).isEqualTo(page)
        assertThat(result.size).isEqualTo(size)
        assertThat(result.totalItems).isEqualTo(totalGuests.toLong())
        assertThat(result.totalPages).isEqualTo(totalGuests)
    }
}
