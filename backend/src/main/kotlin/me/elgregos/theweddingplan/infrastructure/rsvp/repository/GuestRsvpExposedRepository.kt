package me.elgregos.theweddingplan.infrastructure.rsvp.repository

import me.elgregos.theweddingplan.domain.guest.entity.GuestId
import me.elgregos.theweddingplan.domain.rsvp.entity.GuestRsvp
import me.elgregos.theweddingplan.domain.rsvp.entity.GuestRsvpId
import me.elgregos.theweddingplan.domain.rsvp.entity.RsvpAttendance
import me.elgregos.theweddingplan.domain.rsvp.repository.GuestRsvps
import me.elgregos.theweddingplan.infrastructure.guest.repository.GuestTable
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class GuestRsvpExposedRepository: GuestRsvps {

    @Transactional(readOnly = true)
    override fun findByGuestId(guestId: GuestId): GuestRsvp? =
        selectByGuestId(guestId)?.toGuestRsvp()

    @Transactional
    override fun save(rsvp: GuestRsvp): GuestRsvp {
        lockGuest(rsvp.guestId)
        val existing = selectByGuestId(rsvp.guestId)?.toGuestRsvp()
        return if (existing != null) {
            GuestRsvpTable.update({ GuestRsvpTable.guestId eq rsvp.guestId.value }) {
                it[version] = rsvp.version
                it[updateDate] = rsvp.updateDate
                it[attendance] = rsvp.attendance.name
                it[answers] = rsvp.answers
            }
            existing.copy(
                version = rsvp.version,
                updateDate = rsvp.updateDate,
                attendance = rsvp.attendance,
                answers = rsvp.answers,
            )
        } else {
            GuestRsvpTable.insert {
                it[id] = rsvp.id.value
                it[guestId] = rsvp.guestId.value
                it[version] = rsvp.version
                it[creationDate] = rsvp.creationDate
                it[updateDate] = rsvp.updateDate
                it[attendance] = rsvp.attendance.name
                it[answers] = rsvp.answers
            }
            rsvp
        }
    }

    private fun lockGuest(guestId: GuestId) {
        GuestTable.selectAll()
            .where { GuestTable.id eq guestId.value }
            .forUpdate()
            .firstOrNull()
    }

    private fun selectByGuestId(guestId: GuestId): ResultRow? =
        GuestRsvpTable.selectAll()
            .where { GuestRsvpTable.guestId eq guestId.value }
            .firstOrNull()

    private fun ResultRow.toGuestRsvp() = GuestRsvp(
        id = GuestRsvpId(this[GuestRsvpTable.id]),
        guestId = GuestId(this[GuestRsvpTable.guestId]),
        version = this[GuestRsvpTable.version],
        creationDate = this[GuestRsvpTable.creationDate],
        updateDate = this[GuestRsvpTable.updateDate],
        attendance = RsvpAttendance.valueOf(this[GuestRsvpTable.attendance]),
        answers = this[GuestRsvpTable.answers]
    )
}

