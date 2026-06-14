package me.elgregos.theweddingplan.infrastructure.guest

import me.elgregos.theweddingplan.domain.guest.Guest
import me.elgregos.theweddingplan.domain.guest.GuestId
import me.elgregos.theweddingplan.domain.guest.Guests
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class GuestsExposedRepository : Guests {

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
        GuestTable.selectAll().map {
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
}

