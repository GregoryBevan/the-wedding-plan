package me.elgregos.theweddingplan.domain.invitation

import me.elgregos.theweddingplan.domain.guest.GuestId
import me.elgregos.theweddingplan.domain.shared.Dates
import java.time.LocalDateTime

data class Invitation(
    val id: InvitationId = InvitationId.generate(),
    val version: Long = 1L,
    val creationDate: LocalDateTime = Dates.nowUtcMillis(),
    val updateDate: LocalDateTime = Dates.nowUtcMillis(),
    val label: String,
    val description: String,
    val guestIds: Set<GuestId>,
) {
    init {
        require(guestIds.isNotEmpty()) { "An invitation must include at least one guest." }
    }
}

