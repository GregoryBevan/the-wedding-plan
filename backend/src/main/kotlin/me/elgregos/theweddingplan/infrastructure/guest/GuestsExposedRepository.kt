package me.elgregos.theweddingplan.infrastructure.guest

import me.elgregos.theweddingplan.domain.guest.Guest
import me.elgregos.theweddingplan.domain.guest.Guests
import org.jetbrains.exposed.v1.jdbc.insert
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import kotlin.uuid.toKotlinUuid

@Repository
class GuestsExposedRepository : Guests {
    @Transactional
    override fun add(guest: Guest): Guest {
        GuestTable.insert {
            it[id] = guest.id.toKotlinUuid()
            it[version] = guest.version
            it[creationDate] = guest.creationDate
            it[updateDate] = guest.updateDate
            it[firstName] = guest.firstName
            it[lastName] = guest.lastName
            it[email] = guest.email
        }
        return guest
    }
}
