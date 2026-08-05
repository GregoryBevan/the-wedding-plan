package me.elgregos.theweddingplan.infrastructure.rsvp.repository

import me.elgregos.theweddingplan.domain.guest.entity.GuestId
import me.elgregos.theweddingplan.domain.rsvp.entity.GuestRsvp
import me.elgregos.theweddingplan.domain.rsvp.entity.GuestRsvpId
import me.elgregos.theweddingplan.domain.rsvp.entity.RsvpAttendance
import me.elgregos.theweddingplan.domain.rsvp.repository.GuestRsvps
import me.elgregos.theweddingplan.domain.rsvp.repository.PendingSongSync
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import org.jetbrains.exposed.v1.jdbc.upsert
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class GuestRsvpExposedRepository: GuestRsvps {

    @Transactional(readOnly = true)
    override fun findByGuestId(guestId: GuestId): GuestRsvp? =
        selectByGuestId(guestId)?.toGuestRsvp()

    @Transactional(readOnly = true)
    override fun isSongOnPlaylist(deezerId: Long): Boolean =
        GuestRsvpTable.selectAll()
            .any { row -> row[GuestRsvpTable.answers]?.song?.let { it.deezerId == deezerId && it.synchronized } == true }

    @Transactional(readOnly = true)
    override fun isSongChosenByAnyGuest(deezerId: Long): Boolean =
        GuestRsvpTable.selectAll()
            .any { row -> row[GuestRsvpTable.answers]?.song?.deezerId == deezerId }

    @Transactional(readOnly = true)
    override fun findGuestsWithUnsynchronizedSong(): List<PendingSongSync> =
        GuestRsvpTable.selectAll()
            .mapNotNull { row ->
                row[GuestRsvpTable.answers]?.song
                    ?.takeUnless { it.synchronized }
                    ?.let { PendingSongSync(GuestId(row[GuestRsvpTable.guestId]), it) }
            }

    @Transactional
    override fun markSongSynchronized(guestId: GuestId) {
        TransactionManager.current().exec(
            """
            update guest_rsvp
            set answers = jsonb_set(answers, '{song,synchronized}', 'true'::jsonb)
            where guest_id = ?
              and answers -> 'song' ->> 'synchronized' = 'false'
            """.trimIndent(),
            listOf(GuestRsvpTable.guestId.columnType to guestId.value),
        )
    }

    @Transactional
    override fun save(rsvp: GuestRsvp): GuestRsvp {
        GuestRsvpTable.upsert(
            GuestRsvpTable.guestId,
            onUpdateExclude = listOf(GuestRsvpTable.id, GuestRsvpTable.creationDate),
        ) {
            it[id] = rsvp.id.value
            it[guestId] = rsvp.guestId.value
            it[version] = rsvp.version
            it[creationDate] = rsvp.creationDate
            it[updateDate] = rsvp.updateDate
            it[attendance] = rsvp.attendance.name
            it[answers] = rsvp.answers
        }

        return selectByGuestId(rsvp.guestId)?.toGuestRsvp()
            ?: error("RSVP row missing immediately after upsert")
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

