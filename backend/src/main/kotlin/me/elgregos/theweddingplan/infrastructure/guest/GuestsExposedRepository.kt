package me.elgregos.theweddingplan.infrastructure.guest

import me.elgregos.theweddingplan.domain.guest.Guest
import me.elgregos.theweddingplan.domain.guest.GuestId
import me.elgregos.theweddingplan.domain.guest.GuestPage
import me.elgregos.theweddingplan.domain.guest.Guests
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class GuestsExposedRepository : Guests {

    private val listOrder = listOf(GuestTable.creationDate to SortOrder.ASC, GuestTable.id to SortOrder.ASC)

    @Transactional
    override fun add(guest: Guest): Guest =
            GuestTable.insert {
                it[id] = guest.id.value
                it[version] = guest.version
                it[creationDate] = guest.creationDate
                it[updateDate] = guest.updateDate
                it[firstName] = guest.firstName
                it[lastName] = guest.lastName
                it[email] = guest.email
            }.let{ guest }

    @Transactional
    override fun update(guest: Guest): Guest =
            GuestTable.update({ GuestTable.id eq guest.id.value }) {
                it[version] = guest.version
                it[updateDate] = guest.updateDate
                it[firstName] = guest.firstName
                it[lastName] = guest.lastName
                it[email] = guest.email
            }.let{ guest }

    @Transactional(readOnly = true)
    override fun list(): List<Guest> =
        GuestTable.selectAll().orderBy(*listOrder.toTypedArray()).map {
            Guest(
                id = GuestId(it[GuestTable.id]),
                version = it[GuestTable.version],
                creationDate = it[GuestTable.creationDate],
                updateDate = it[GuestTable.updateDate],
                firstName = it[GuestTable.firstName],
                lastName = it[GuestTable.lastName],
                email = it[GuestTable.email]
            )
        }

    @Transactional(readOnly = true)
    override fun list(page: Int, size: Int): GuestPage {
        val totalItems = GuestTable.selectAll().count()
        val totalPages = if (totalItems == 0L) 0 else ((totalItems - 1) / size + 1).toInt()
        val offset = page.toLong() * size

        val items = GuestTable.selectAll()
            .orderBy(*listOrder.toTypedArray())
            .limit(size)
            .offset(offset)
            .map {
                Guest(
                    id = GuestId(it[GuestTable.id]),
                    version = it[GuestTable.version],
                    creationDate = it[GuestTable.creationDate],
                    updateDate = it[GuestTable.updateDate],
                    firstName = it[GuestTable.firstName],
                    lastName = it[GuestTable.lastName],
                    email = it[GuestTable.email]
                )
            }

        return GuestPage(items = items, page = page, size = size, totalItems = totalItems, totalPages = totalPages)
    }
}

