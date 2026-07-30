package me.elgregos.theweddingplan.application.rsvp

import me.elgregos.theweddingplan.application.rsvp.command.SubmitGuestRsvpCommand
import me.elgregos.theweddingplan.application.rsvp.result.SubmitGuestRsvpResult
import me.elgregos.theweddingplan.domain.rsvp.entity.GuestRsvp
import me.elgregos.theweddingplan.domain.rsvp.repository.GuestRsvps
import org.springframework.stereotype.Service

@Service
class GuestRsvpSubmitter(private val guestRsvps: GuestRsvps) {

    fun submit(command: SubmitGuestRsvpCommand): SubmitGuestRsvpResult =
        guestRsvps.findByGuestId(command.guestId)
            ?.let { SubmitGuestRsvpResult.Updated(guestRsvps.save(it.respond(command.attendance))) }
            ?: SubmitGuestRsvpResult.Created(
                guestRsvps.save(GuestRsvp(guestId = command.guestId, attendance = command.attendance))
            )
}

