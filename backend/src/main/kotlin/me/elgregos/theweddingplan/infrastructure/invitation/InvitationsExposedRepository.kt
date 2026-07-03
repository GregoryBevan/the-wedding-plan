package me.elgregos.theweddingplan.infrastructure.invitation

import me.elgregos.theweddingplan.domain.guest.GuestId
import me.elgregos.theweddingplan.domain.invitation.*
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

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

        InvitationGuestTable.batchInsert(invitation.guestIds) { guestId ->
            this[InvitationGuestTable.invitationId] = invitation.id.value
            this[InvitationGuestTable.guestId] = guestId.value
        }

        return invitation
    }

    @Transactional(readOnly = true)
    override fun findById(id: InvitationId): Invitation? =
        InvitationTable.selectAll()
            .where { InvitationTable.id eq id.value }
            .firstOrNull()
            ?.toInvitation(fetchGuestIds(setOf(id.value))[id.value].orEmpty())

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
        val guestIdsByInvitation = fetchGuestIds(invitationIds)

        return InvitationPage(
            items = rows.map { row -> row.toInvitation(guestIdsByInvitation[row[InvitationTable.id]].orEmpty()) },
            page = criteria.page,
            size = criteria.size,
            totalItems = totalItems,
            totalPages = totalPages,
        )
    }

    private fun fetchGuestIds(invitationIds: Set<kotlin.uuid.Uuid>) =
        if (invitationIds.isEmpty()) {
            emptyMap()
        } else {
            InvitationGuestTable.selectAll()
                .where { InvitationGuestTable.invitationId inList invitationIds }
                .groupBy({ it[InvitationGuestTable.invitationId] }) { GuestId(it[InvitationGuestTable.guestId]) }
                .mapValues { (_, guestIds) -> guestIds.toSet() }
        }

    private fun ResultRow.toInvitation(guestIds: Set<GuestId>) = Invitation(
        id = InvitationId(this[InvitationTable.id]),
        version = this[InvitationTable.version],
        creationDate = this[InvitationTable.creationDate],
        updateDate = this[InvitationTable.updateDate],
        label = this[InvitationTable.label],
        description = this[InvitationTable.description],
        guestIds = guestIds,
    )
}

