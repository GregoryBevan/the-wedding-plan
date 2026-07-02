package me.elgregos.theweddingplan.application.invitation

import me.elgregos.theweddingplan.domain.guest.GuestId
import me.elgregos.theweddingplan.domain.guest.Guests
import me.elgregos.theweddingplan.domain.invitation.Invitations
import org.springframework.stereotype.Service

@Service
class InvitationAdder(
    private val invitations: Invitations,
    private val guests: Guests,
) {

    fun add(command: AddInvitationCommand): AddInvitationResult =
        with(command.guestIds) {
            if (isEmpty()) AddInvitationResult.MissingGuests
            else filterNot(::isActiveGuest).toSet().let { invalidGuests ->
                    if (invalidGuests.isNotEmpty()) {
                        AddInvitationResult.InvalidGuests(invalidGuests)
                    } else {
                        AddInvitationResult.Added(invitations.add(command.toInvitation()))
                    }
                }
        }

    private fun isActiveGuest(id: GuestId) = guests.findById(id) != null
}

