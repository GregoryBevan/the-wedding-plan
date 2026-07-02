package me.elgregos.theweddingplan.infrastructure.guest

import assertk.assertThat
import assertk.assertions.containsAtLeast
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import me.elgregos.theweddingplan.AbstractIntegrationTest
import me.elgregos.theweddingplan.domain.guest.Guest
import me.elgregos.theweddingplan.domain.guest.GuestStatus
import me.elgregos.theweddingplan.domain.guest.GuestListCriteria
import me.elgregos.theweddingplan.domain.guest.GuestFixtures
import me.elgregos.theweddingplan.domain.guest.GuestFixtures.janeDoe
import me.elgregos.theweddingplan.domain.guest.GuestFixtures.johnDoe
import me.elgregos.theweddingplan.domain.guest.GuestFixtures.liamMiller
import me.elgregos.theweddingplan.domain.guest.GuestFixtures.liamMillerUpdated
import me.elgregos.theweddingplan.domain.guest.GuestFixtures.noahAnderson
import me.elgregos.theweddingplan.domain.guest.GuestFixtures.noahAndersonUpdated
import me.elgregos.theweddingplan.domain.guest.GuestId
import me.elgregos.theweddingplan.domain.guest.GuestPage
import me.elgregos.theweddingplan.domain.shared.Dates
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import java.util.*
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
        guestsRepository.add(liamMiller)

        guestsRepository.update(liamMillerUpdated, expectedVersion = liamMiller.version)

        assertThat(guestById(liamMillerUpdated.id)).isEqualTo(liamMillerUpdated)
    }

    @Test
    fun `should return null when expected version does not match`() {
        guestsRepository.add(noahAnderson)

        val result = guestsRepository.update(noahAndersonUpdated, expectedVersion = noahAndersonUpdated.version + 5)
        val persistedGuest = guestById(noahAndersonUpdated.id)

        assertThat(result).isNull()
        assertThat(persistedGuest).isEqualTo(noahAnderson)
    }

    @Test
    fun `should return null when trying to update an archived guest`() {
        val guest = Guest(firstName = "Ryan", lastName = "Evans", email = "ryanevans@teleworm.us")
        guestsRepository.add(guest)
        markAsArchived(guest)

        val result = guestsRepository.update(
            guest.copy(firstName = "Updated"),
            expectedVersion = guest.version,
        )

        assertThat(result).isNull()
    }

    @Test
    fun `should list all guests`() {
        val guests = guestsRepository.list(GuestListCriteria(page = 0, size = 5, status = GuestStatus.ACTIVE))

        assertThat(guests.items).containsAtLeast(johnDoe, janeDoe)
    }

    @Test
    fun `should list only active guests`() {
        val archivedGuest = Guest(firstName = "Joyce", lastName = "Clement", email = "joyceclement@example.com")
        guestsRepository.add(archivedGuest)
        markAsArchived(archivedGuest)

        val guests = guestsRepository.list(GuestListCriteria(status = GuestStatus.ACTIVE))

        assertThat(guests.items.any { it.id == archivedGuest.id }).isFalse()
    }

    @Test
    fun `should list only archived guests from trash query`() {
        val activeGuest = Guest(firstName = "Ryan", lastName = "Evans", email = "ryanevans@teleworm.us")
        guestsRepository.add(activeGuest)
        val archivedGuest =Guest(firstName = "Julianne", lastName = "Whitaker", email = "juliannewhitaker@jourrapide.com")
        guestsRepository.add(archivedGuest)
        markAsArchived(archivedGuest)

        val archivedGuests = guestsRepository.list(GuestListCriteria(status = GuestStatus.ARCHIVED))

        assertThat(archivedGuests.items.any { it.id == archivedGuest.id }).isTrue()
        assertThat(archivedGuests.items.any { it.id == activeGuest.id }).isFalse()
    }

    @Test
    fun `should list guests with pagination`() {
        val totalGuests = guestCount()

        val firstPage = guestsRepository.list(GuestListCriteria(page = 0, size = 1))
        val secondPage = guestsRepository.list(GuestListCriteria(page = 1, size = 1))

        assertThat(firstPage.items).isEqualTo(listOf(johnDoe))
        assertPageMetadata(page = 0, totalGuests = totalGuests, result = firstPage)

        assertThat(secondPage.items).isEqualTo(listOf(janeDoe))
        assertPageMetadata(page = 1, totalGuests = totalGuests, result = secondPage)
    }

    @Test
    fun `should find guest by id`() {
        val guest = guestsRepository.findById(johnDoe.id)

        assertThat(guest).isEqualTo(johnDoe)
    }

    @Test
    fun `should return null for an archived guest`() {
        val guest = Guest(firstName = "Joyce", lastName = "Clement", email = "joyceclement@example.com")
        guestsRepository.add(guest)
        markAsArchived(guest)

        val found = guestsRepository.findById(guest.id)

        assertThat(found).isNull()
    }

    @Test
    fun `should restore an archived guest`() {
        val guest = Guest(firstName = "Restore", lastName = "Candidate", email = "restore.candidate@example.com")
        guestsRepository.add(guest)
        markAsArchived(guest)

        val archivedGuest = guestById(guest.id)
        val restoredGuest = guestsRepository.restore(archivedGuest.restore(now = Dates.nowUtcMillis()), expectedVersion = archivedGuest.version)
        val found = guestsRepository.findById(guest.id)

        assertThat(restoredGuest).isNotNull()
        assertThat(found).isNotNull()
        assertThat(found?.deletionDate).isNull()
    }

    @Test
    fun `should return null when guest id does not exist`() {
        val missingGuest = guestsRepository.findById(GuestId.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a99"))

        assertThat(missingGuest).isNull()
    }

    @Test
    fun `should persist deletion date when guest is marked as archived`() {
        val guest = Guest(firstName = "Sarah", lastName = "Mills", email = "sarahmills@example.com")
        guestsRepository.add(guest)

        markAsArchived(guest)
        val archivedGuest = guestById(guest.id)

        assertThat(archivedGuest.deletionDate).isNotNull()
    }

    private fun guestCount() =
        jdbcTemplate.queryForObject(
            "select count(*) from guest where deletion_date is null",
            Int::class.java
        ) ?: 0

    private fun guestById(guestId: GuestId): Guest =
        jdbcTemplate.queryForObject(
            """
            select id, version, creation_date, update_date, deletion_date, first_name, last_name, email
            from guest
            where id = ?
            """.trimIndent(),
            { rs, _ ->
                Guest(
                    id = GuestId.fromString(rs.getObject("id", UUID::class.java).toString()),
                    version = rs.getLong("version"),
                    creationDate = rs.getTimestamp("creation_date").toLocalDateTime(),
                    updateDate = rs.getTimestamp("update_date").toLocalDateTime(),
                    deletionDate = rs.getTimestamp("deletion_date")?.toLocalDateTime(),
                    firstName = rs.getString("first_name"),
                    lastName = rs.getString("last_name"),
                    email = rs.getString("email")
                )
            },
            guestId.value.toJavaUuid()
        )

    private fun markAsArchived(guest: Guest) {
        guestsRepository.update(
            guest.markAsArchived(now = Dates.nowUtcMillis()),
            expectedVersion = guest.version,
        )
    }

    private fun assertPageMetadata(page: Int, size: Int = 1, totalGuests: Int, result: GuestPage) {
        assertThat(result.page).isEqualTo(page)
        assertThat(result.size).isEqualTo(size)
        assertThat(result.totalItems).isEqualTo(totalGuests.toLong())
        assertThat(result.totalPages).isEqualTo(totalGuests)
    }
}
