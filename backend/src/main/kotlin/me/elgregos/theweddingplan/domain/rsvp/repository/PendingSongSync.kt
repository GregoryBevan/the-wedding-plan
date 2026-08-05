package me.elgregos.theweddingplan.domain.rsvp.repository

import me.elgregos.theweddingplan.domain.guest.entity.GuestId
import me.elgregos.theweddingplan.domain.rsvp.entity.SongChoice

data class PendingSongSync(
    val guestId: GuestId,
    val song: SongChoice,
)

