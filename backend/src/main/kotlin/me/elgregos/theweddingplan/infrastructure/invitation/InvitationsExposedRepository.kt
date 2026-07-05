package me.elgregos.theweddingplan.infrastructure.invitation

import me.elgregos.theweddingplan.domain.guest.Guest
import me.elgregos.theweddingplan.domain.guest.GuestId
import me.elgregos.theweddingplan.domain.invitation.Invitation
import me.elgregos.theweddingplan.domain.invitation.InvitationId
import me.elgregos.theweddingplan.domain.invitation.InvitationListCriteria
import me.elgregos.theweddingplan.domain.invitation.InvitationPage
import me.elgregos.theweddingplan.domain.invitation.Invitations
import me.elgregos.theweddingplan.infrastructure.guest.GuestTable
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
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
        }

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
        guestIds
            .map(GuestId::value)
            .takeIf { it.isNotEmpty() }
            ?.let { candidateIds ->
                InvitationGuestTable.selectAll()
                    .where { InvitationGuestTable.guestId inList candidateIds }
                    .map { GuestId(it[InvitationGuestTable.guestId]) }
                    .toSet()
            }
            ?: emptySet()

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
    )
}

