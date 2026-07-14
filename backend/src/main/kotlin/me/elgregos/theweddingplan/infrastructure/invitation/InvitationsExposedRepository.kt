package me.elgregos.theweddingplan.infrastructure.invitation

import me.elgregos.theweddingplan.domain.guest.Guest
import me.elgregos.theweddingplan.domain.guest.GuestId
import me.elgregos.theweddingplan.domain.invitation.*
import me.elgregos.theweddingplan.infrastructure.guest.GuestTable
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import kotlin.uuid.Uuid

@Repository
class InvitationsExposedRepository : Invitations {

    private val listOrder =
        arrayOf(InvitationTable.id to SortOrder.ASC)

    @Transactional
    override fun add(invitation: Invitation): Invitation {
        InvitationTable.insert {
            it[id] = invitation.id.value
            it[version] = invitation.version
            it[creationDate] = invitation.creationDate
            it[updateDate] = invitation.updateDate
            it[label] = invitation.label
            it[description] = invitation.description
            it[accessToken] = invitation.accessToken.value
        }

        InvitationGuestTable.batchInsert(invitation.guests) { guest ->
            this[InvitationGuestTable.invitationId] = invitation.id.value
            this[InvitationGuestTable.guestId] = guest.id.value
        }

        return invitation
    }

    @Transactional
    override fun update(invitation: Invitation): Invitation? {
        val updatedRows = InvitationTable.update({ InvitationTable.id eq invitation.id.value }) {
            it[version] = invitation.version
            it[updateDate] = invitation.updateDate
            it[label] = invitation.label
            it[description] = invitation.description
        }

        if (updatedRows == 0) {
            return null
        }

        InvitationGuestTable.deleteWhere { invitationId eq invitation.id.value }
        InvitationGuestTable.batchInsert(invitation.guests) { guest ->
            this[InvitationGuestTable.invitationId] = invitation.id.value
            this[InvitationGuestTable.guestId] = guest.id.value
        }

        return invitation
    }

    @Transactional(readOnly = true)
    override fun findById(id: InvitationId): Invitation? =
        InvitationTable.selectAll()
            .where { InvitationTable.id eq id.value }
            .firstOrNull()
            ?.toInvitation(fetchGuestsByInvitationIds(setOf(id.value))[id.value].orEmpty())

    @Transactional(readOnly = true)
    override fun list(criteria: InvitationListCriteria): InvitationPage {
        val totalItems = InvitationTable.selectAll().count()
        val totalPages = if (totalItems == 0L) 0 else ((totalItems - 1) / criteria.size + 1).toInt()
        val offset = criteria.page.toLong() * criteria.size

        val rows = InvitationTable.selectAll()
            .orderBy(*listOrder)
            .limit(criteria.size)
            .offset(offset)
            .toList()

        val invitationIds = rows.map { it[InvitationTable.id] }.toSet()
        val guestsByInvitation = fetchGuestsByInvitationIds(invitationIds)

        return InvitationPage(
            items = rows.map { row -> row.toInvitation(guestsByInvitation[row[InvitationTable.id]].orEmpty()) },
            page = criteria.page,
            size = criteria.size,
            totalItems = totalItems,
            totalPages = totalPages,
        )
    }

    @Transactional(readOnly = true)
    override fun findAssignedGuestIds(guestIds: Set<GuestId>): Set<GuestId> =
        if (guestIds.isEmpty()) emptySet()
        else InvitationGuestTable.selectAll()
            .where { InvitationGuestTable.guestId inList guestIds.map(GuestId::value) }
            .map { GuestId(it[InvitationGuestTable.guestId]) }
            .toSet()


    @Transactional(readOnly = true)
    override fun findInvitationByAccessToken(token: InvitationAccessToken): Invitation? =
        InvitationTable.selectAll()
            .where { InvitationTable.accessToken eq token.value }
            .firstOrNull()
            ?.let { row ->
                row[InvitationTable.id]
                    .let { invitationId ->
                        row.toInvitation(fetchGuestsByInvitationIds(setOf(invitationId))[invitationId].orEmpty())
                    }
            }

    private fun fetchGuestsByInvitationIds(invitationIds: Set<Uuid>) =
        if (invitationIds.isEmpty()) {
            emptyMap()
        } else {
            InvitationGuestTable
                .join(GuestTable, JoinType.INNER, InvitationGuestTable.guestId, GuestTable.id)
                .selectAll()
                .where { InvitationGuestTable.invitationId inList invitationIds }
                .groupBy({ it[InvitationGuestTable.invitationId] }) { row ->
                    Guest(
                        id = GuestId(row[GuestTable.id]),
                        version = row[GuestTable.version],
                        creationDate = row[GuestTable.creationDate],
                        updateDate = row[GuestTable.updateDate],
                        deletionDate = row[GuestTable.deletionDate],
                        firstName = row[GuestTable.firstName],
                        lastName = row[GuestTable.lastName],
                        email = row[GuestTable.email],
                    )
                }
                .mapValues { (_, guests) -> guests.toSet() }
        }

    private fun ResultRow.toInvitation(guests: Set<Guest>) = Invitation(
        id = InvitationId(this[InvitationTable.id]),
        version = this[InvitationTable.version],
        creationDate = this[InvitationTable.creationDate],
        updateDate = this[InvitationTable.updateDate],
        label = this[InvitationTable.label],
        description = this[InvitationTable.description],
        guests = guests,
        accessToken = InvitationAccessToken(this[InvitationTable.accessToken]),
    )
}

