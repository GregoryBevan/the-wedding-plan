package me.elgregos.theweddingplan.infrastructure.guest

import me.elgregos.theweddingplan.domain.guest.Guest
import me.elgregos.theweddingplan.domain.guest.GuestStatus
import me.elgregos.theweddingplan.domain.guest.GuestListCriteria
import me.elgregos.theweddingplan.domain.guest.GuestId
import me.elgregos.theweddingplan.domain.guest.GuestPage
import me.elgregos.theweddingplan.domain.guest.Guests
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.core.isNotNull
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.jdbc.Query
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class GuestExposedRepository : Guests {

    private val listOrder = arrayOf(GuestTable.id to SortOrder.ASC)

    @Transactional
    override fun add(guest: Guest): Guest =
        GuestTable.insert {
            it[id] = guest.id.value
            it[version] = guest.version
            it[creationDate] = guest.creationDate
            it[updateDate] = guest.updateDate
            it[deletionDate] = guest.deletionDate
            it[firstName] = guest.firstName
            it[lastName] = guest.lastName
            it[email] = guest.email
        }.let { guest }

    @Transactional
    override fun update(guest: Guest, expectedVersion: Long): Guest? =
        GuestTable.update({ (GuestTable.id eq guest.id.value) and (GuestTable.version eq expectedVersion) and GuestTable.deletionDate.isNull() }) {
            it[version] = guest.version
            it[updateDate] = guest.updateDate
            it[deletionDate] = guest.deletionDate
            it[firstName] = guest.firstName
            it[lastName] = guest.lastName
            it[email] = guest.email
        }.let { if (it == 1) guest else null }


    @Transactional(readOnly = true)
    override fun findById(id: GuestId): Guest? =
        GuestTable.selectAll()
            .where { (GuestTable.id eq id.value) and GuestTable.deletionDate.isNull() }
            .firstOrNull()
            ?.toGuest()

    @Transactional(readOnly = true)
    override fun findByIds(ids: Set<GuestId>): Set<Guest> =
        if (ids.isEmpty()) {
            emptySet()
        } else {
            GuestTable.selectAll()
                .where { (GuestTable.id inList ids.map(GuestId::value)) and GuestTable.deletionDate.isNull() }
                .map { it.toGuest() }
                .toSet()
        }

    @Transactional(readOnly = true)
    override fun findArchivedById(id: GuestId): Guest? =
        GuestTable.selectAll()
            .where { (GuestTable.id eq id.value) and GuestTable.deletionDate.isNotNull() }
            .firstOrNull()
            ?.toGuest()

    @Transactional
    override fun restore(guest: Guest, expectedVersion: Long): Guest? =
        GuestTable.update({ (GuestTable.id eq guest.id.value) and (GuestTable.version eq expectedVersion) and GuestTable.deletionDate.isNotNull() }) {
            it[version] = guest.version
            it[updateDate] = guest.updateDate
            it[deletionDate] = guest.deletionDate
            it[firstName] = guest.firstName
            it[lastName] = guest.lastName
            it[email] = guest.email
        }.let { if (it == 1) guest else null }

    @Transactional(readOnly = true)
    override fun list(criteria: GuestListCriteria): GuestPage {
        val totalItems = GuestTable.selectAll().applyStatusFilter(criteria.status).count()
        val totalPages = if (totalItems == 0L) 0 else ((totalItems - 1) / criteria.size + 1).toInt()
        val offset = criteria.page.toLong() * criteria.size

        return GuestPage(
            items = selectGuests(criteria, offset),
            page = criteria.page,
            size = criteria.size,
            totalItems = totalItems,
            totalPages = totalPages,
        )
    }

    private fun ResultRow.toGuest() = Guest(
        id = GuestId(this[GuestTable.id]),
        version = this[GuestTable.version],
        creationDate = this[GuestTable.creationDate],
        updateDate = this[GuestTable.updateDate],
        deletionDate = this[GuestTable.deletionDate],
        firstName = this[GuestTable.firstName],
        lastName = this[GuestTable.lastName],
        email = this[GuestTable.email],
    )

    private fun selectGuests(
        criteria: GuestListCriteria,
        offset: Long
    ): List<Guest> = GuestTable.selectAll()
        .applyStatusFilter(criteria.status)
        .orderBy(*listOrder)
        .limit(criteria.size)
        .offset(offset)
        .map { it.toGuest() }

    private fun Query.applyStatusFilter(status: GuestStatus) =
        when (status) {
            GuestStatus.ACTIVE -> where { GuestTable.deletionDate.isNull() }
            GuestStatus.ARCHIVED -> where { GuestTable.deletionDate.isNotNull() }
            GuestStatus.ALL -> this
        }
}
