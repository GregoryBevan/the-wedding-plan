package me.elgregos.theweddingplan.infrastructure.invitation

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import me.elgregos.theweddingplan.AbstractIntegrationTest
import me.elgregos.theweddingplan.domain.guest.Guest
import me.elgregos.theweddingplan.domain.guest.GuestFixtures.albertEinstein
import me.elgregos.theweddingplan.domain.guest.GuestFixtures.emmaWilson
import me.elgregos.theweddingplan.domain.guest.GuestFixtures.janeDoe
import me.elgregos.theweddingplan.domain.guest.GuestFixtures.liamMiller
import me.elgregos.theweddingplan.domain.guest.GuestFixtures.marieCurie
import me.elgregos.theweddingplan.domain.guest.GuestFixtures.pierreCurie
import me.elgregos.theweddingplan.domain.guest.GuestId
import me.elgregos.theweddingplan.domain.guest.Guests
import me.elgregos.theweddingplan.domain.invitation.*
import me.elgregos.theweddingplan.domain.invitation.InvitationFixtures.bestManInvitation
import me.elgregos.theweddingplan.domain.invitation.InvitationFixtures.bridesMaidInvitation
import me.elgregos.theweddingplan.domain.invitation.InvitationFixtures.friendsInvitation
import me.elgregos.theweddingplan.domain.invitation.InvitationFixtures.nonExistingInvitation
import me.elgregos.theweddingplan.domain.invitation.InvitationFixtures.scienceConferenceInvitation
import me.elgregos.theweddingplan.domain.invitation.InvitationFixtures.scienceConferenceInvitationUpdated
import me.elgregos.theweddingplan.domain.shared.Dates.nowUtcMillis
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.jdbc.core.JdbcTemplate
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.uuid.toJavaUuid

class InvitationsExposedRepositoryIT : AbstractIntegrationTest() {

    @Autowired
    private lateinit var invitationsRepository: Invitations

    @Autowired
    private lateinit var guestRepository: Guests

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Test
    fun `should insert invitation with multiple guests`() {
        val initialCount = invitationCount()

        guestRepository.add(emmaWilson)
        guestRepository.add(liamMiller)
        val createdInvitation = invitationsRepository.add(friendsInvitation)

        assertThat(invitationCount()).isEqualTo(initialCount + 1)
        assertThat(invitationById(friendsInvitation.id)).isEqualTo(createdInvitation)
    }

    @Test
    fun `should update existing invitation details and guests`() {
        guestRepository.add(albertEinstein)
        guestRepository.add(marieCurie)

        val invitation = scienceConferenceInvitation
        val createdInvitation = invitationsRepository.add(invitation)
        val initialCount = invitationCount()

        val invitationToUpdate =scienceConferenceInvitationUpdated

        val updatedInvitation = invitationsRepository.update(invitationToUpdate)

        assertThat(updatedInvitation).isEqualTo(invitationToUpdate)
        assertThat(invitationsRepository.findById(createdInvitation.id)).isEqualTo(invitationToUpdate)
        assertThat(invitationCount()).isEqualTo(initialCount)
        assertThat(invitationsRepository.findAssignedGuestIds(setOf(albertEinstein.id))).isEqualTo(emptySet())
        assertThat(invitationsRepository.findAssignedGuestIds(setOf(marieCurie.id))).isEqualTo(setOf(marieCurie.id))
    }

    @Test
    fun `should return null when trying to update non-existing invitation`() {
        val guest = pierreCurie
        guestRepository.add(guest)

        val nonExisting = nonExistingInvitation(guest, updateDate = nowUtcMillis())

        val updatedInvitation = invitationsRepository.update(nonExisting)

        assertThat(updatedInvitation).isNull()
    }

    @Test
    fun `should find invitation by id`() {
        val found = invitationsRepository.findById(bridesMaidInvitation.id)

        assertThat(found).isEqualTo(bridesMaidInvitation)
    }

    @Test
    fun `should return null when invitation id does not exist`() {
        val missing = invitationsRepository.findById(InvitationId.fromString("019f6c83-cd2d-7357-833b-e7cda750ba32"))

        assertThat(missing).isNull()
    }

    @Test
    fun `should list invitations with pagination and id ordering`() {
        val itemCount = invitationCount()
        val firstPage = invitationsRepository.list(InvitationListCriteria(page = 0, size = 1))
        val secondPage = invitationsRepository.list(InvitationListCriteria(page = 1, size = 1))

        assertThat(firstPage.items).isEqualTo(listOf(bridesMaidInvitation))
        assertThat(firstPage.page).isEqualTo(0)
        assertThat(firstPage.size).isEqualTo(1)
        assertThat(firstPage.totalItems).isEqualTo(itemCount.toLong())
        assertThat(firstPage.totalPages).isEqualTo(itemCount)

        assertThat(secondPage.items).isEqualTo(listOf(bestManInvitation))
        assertThat(secondPage.page).isEqualTo(1)
        assertThat(secondPage.size).isEqualTo(1)
        assertThat(secondPage.totalItems).isEqualTo(itemCount.toLong())
        assertThat(secondPage.totalPages).isEqualTo(itemCount)
    }

    @Test
    fun `should return empty items when page has no invitations`() {
        val itemCount = invitationCount()

        val outOfRangePage = invitationsRepository.list(InvitationListCriteria(page = itemCount, size = 1))

        assertThat(outOfRangePage.items).isEqualTo(emptyList())
        assertThat(outOfRangePage.page).isEqualTo(itemCount)
        assertThat(outOfRangePage.size).isEqualTo(1)
        assertThat(outOfRangePage.totalItems).isEqualTo(itemCount.toLong())
        assertThat(outOfRangePage.totalPages).isEqualTo(itemCount)
    }

    @Test
    fun `should reject duplicate guest assignment across invitations`() {
        assertFailsWith<DataIntegrityViolationException> {
            jdbcTemplate.update(
                """
                insert into invitation_guest (invitation_id, guest_id)
                values (?, ?)
                """.trimIndent(),
                bestManInvitation.id.value.toJavaUuid(),
                janeDoe.id.value.toJavaUuid(),
            )
        }
    }

    @Test
    fun `should find assigned guest ids in batch`() {
        val unassignedGuestId = GuestId.fromString("019fa9fa-b235-75f8-a499-2c8ce31e1e6c")

        val assignedGuestIds = invitationsRepository.findAssignedGuestIds(setOf(janeDoe.id, unassignedGuestId))

        assertThat(assignedGuestIds).isEqualTo(setOf(janeDoe.id))
    }

    @Test
    fun `should resolve invitation by access token`() {
        val found = invitationsRepository.findInvitationByAccessToken(bridesMaidInvitation.accessToken)

        assertThat(found).isEqualTo(bridesMaidInvitation)
    }

    @Test
    fun `should reject duplicate access token values at database level`() {
        assertFailsWith<DataIntegrityViolationException> {
            jdbcTemplate.update(
                """
                update invitation
                set access_token = ?
                where id = ?
                """.trimIndent(),
                bridesMaidInvitation.accessToken.value,
                bestManInvitation.id.value.toJavaUuid(),
            )
        }
    }

    private fun invitationCount() =
        jdbcTemplate.queryForObject("select count(*) from invitation", Int::class.java) ?: 0

    private fun invitationById(invitationId: InvitationId): Invitation =
        jdbcTemplate.queryForObject(
            """
            select i.id,
                   i.version,
                   i.creation_date,
                   i.update_date,
                   i.label,
                   i.description,
                   i.access_token,
                   array_agg(g.id order by g.id) as guest_ids,
                   array_agg(g.version order by g.id) as guest_versions,
                   array_agg(g.creation_date order by g.id) as guest_creation_dates,
                   array_agg(g.update_date order by g.id) as guest_update_dates,
                   array_agg(g.deletion_date order by g.id) as guest_deletion_dates,
                   array_agg(g.first_name order by g.id) as guest_first_names,
                   array_agg(g.last_name order by g.id) as guest_last_names,
                   array_agg(g.email order by g.id) as guest_emails
            from invitation i
            inner join invitation_guest ig on ig.invitation_id = i.id
            inner join guest g on g.id = ig.guest_id
            where i.id = ?
            group by i.id, i.version, i.creation_date, i.update_date, i.label, i.description, i.access_token
        """.trimIndent(),
            { rs, _ ->
                val guestIds = (rs.getArray("guest_ids").array as Array<*>).map { GuestId.fromString(it.toString()) }
                val guestVersions = (rs.getArray("guest_versions").array as Array<*>).map { (it as Number).toLong() }
                val guestCreationDates = (rs.getArray("guest_creation_dates").array as Array<*>).map { (it as java.sql.Timestamp).toLocalDateTime() }
                val guestUpdateDates = (rs.getArray("guest_update_dates").array as Array<*>).map { (it as java.sql.Timestamp).toLocalDateTime() }
                val guestDeletionDates = (rs.getArray("guest_deletion_dates").array as Array<*>).map { (it as java.sql.Timestamp?)?.toLocalDateTime() }
                val guestFirstNames = (rs.getArray("guest_first_names").array as Array<*>).map { it.toString() }
                val guestLastNames = (rs.getArray("guest_last_names").array as Array<*>).map { it.toString() }
                val guestEmails = (rs.getArray("guest_emails").array as Array<*>).map { it.toString() }

                val guests = guestIds.indices.map { index ->
                    Guest(
                        id = guestIds[index],
                        version = guestVersions[index],
                        creationDate = guestCreationDates[index],
                        updateDate = guestUpdateDates[index],
                        deletionDate = guestDeletionDates[index],
                        firstName = guestFirstNames[index],
                        lastName = guestLastNames[index],
                        email = guestEmails[index],
                    )
                }.toSet()

                Invitation(
                    id = InvitationId.fromString(rs.getString("id")),
                    version = rs.getLong("version"),
                    creationDate = rs.getTimestamp("creation_date").toLocalDateTime(),
                    updateDate = rs.getTimestamp("update_date").toLocalDateTime(),
                    label = rs.getString("label"),
                    description = rs.getString("description"),
                    guests = guests,
                    accessToken = InvitationAccessToken(rs.getString("access_token")),
                )
            },
            invitationId.value.toJavaUuid())

}

