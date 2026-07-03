package me.elgregos.theweddingplan.infrastructure.invitation

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import me.elgregos.theweddingplan.AbstractIntegrationTest
import me.elgregos.theweddingplan.domain.guest.GuestFixtures.emmaWilson
import me.elgregos.theweddingplan.domain.guest.GuestFixtures.liamMiller
import me.elgregos.theweddingplan.domain.guest.Guests
import me.elgregos.theweddingplan.domain.invitation.Invitation
import me.elgregos.theweddingplan.domain.invitation.InvitationFixtures.bestManInvitation
import me.elgregos.theweddingplan.domain.invitation.InvitationFixtures.bridesMaidInvitation
import me.elgregos.theweddingplan.domain.invitation.InvitationFixtures.friendsInvitation
import me.elgregos.theweddingplan.domain.invitation.InvitationId
import me.elgregos.theweddingplan.domain.invitation.InvitationListCriteria
import me.elgregos.theweddingplan.domain.invitation.Invitations
import me.elgregos.theweddingplan.shared.getUuidSet
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import kotlin.test.Test
import kotlin.uuid.toJavaUuid

class InvitationExposedRepositoryIT : AbstractIntegrationTest() {

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
        invitationsRepository.add(friendsInvitation)

        assertThat(invitationCount()).isEqualTo(initialCount + 1)
        val persisted = invitationById(friendsInvitation.id)

        assertThat(persisted).isEqualTo(friendsInvitation)
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

    private fun invitationCount() =
        jdbcTemplate.queryForObject("select count(*) from invitation", Int::class.java) ?: 0

    private fun invitationById(invitationId: InvitationId): Invitation =
        jdbcTemplate.queryForObject(
            """
            select i.id, i.version, i.creation_date, i.update_date, i.label, i.description, array_agg(ig.guest_id) as guests
            from invitation i
            inner join invitation_guest ig on ig.invitation_id = i.id
            where i.id = ?
            group by i.id
        """.trimIndent(),
            { rs, _ ->                          // ← RowMapper lambda, not Class<T>
                Invitation(
                    id = InvitationId.fromString(rs.getString("id")),
                    version = rs.getLong("version"),
                    creationDate = rs.getTimestamp("creation_date").toLocalDateTime(),
                    updateDate = rs.getTimestamp("update_date").toLocalDateTime(),
                    label = rs.getString("label"),
                    description = rs.getString("description"),
                    guestIds = rs.getUuidSet("guests")
                )
            },
            invitationId.value.toJavaUuid())
}

