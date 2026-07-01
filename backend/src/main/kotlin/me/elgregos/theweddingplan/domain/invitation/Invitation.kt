package me.elgregos.theweddingplan.domain.invitation

import me.elgregos.theweddingplan.domain.guest.GuestId
import me.elgregos.theweddingplan.domain.shared.Dates
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
data class Invitation(
    val id: InvitationId = InvitationId.generate(),
    val creationDate: LocalDateTime = Dates.nowUtcMillis(),
    val updateDate: LocalDateTime = Dates.nowUtcMillis(),
    val label: String,
    val eventDate: LocalDate? = null,
    val guestIds: Set<GuestId>,
) {
    init {
        require(guestIds.isNotEmpty()) { "An invitation must include at least one guest." }
    }
}

