package me.elgregos.theweddingplan.application.invitation

import me.elgregos.theweddingplan.domain.guest.Guest
import me.elgregos.theweddingplan.domain.guest.GuestId
import me.elgregos.theweddingplan.domain.guest.Guests
import me.elgregos.theweddingplan.domain.invitation.Invitation
import me.elgregos.theweddingplan.domain.invitation.Invitations
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service

@Service
class InvitationUpdater(
    private val invitations: Invitations,
    private val guests: Guests,
) {

    fun update(command: UpdateInvitationCommand): UpdateInvitationResult =
        // Prefer explicit, easy-to-read control flow over nested scope functions
        invitations.findById(command.invitationId)?.let { currentInvitation ->
            if (command.guestIds.isEmpty()) return UpdateInvitationResult.MissingGuests

            val activeGuests = guests.findByIds(command.guestIds)

            val invalid = invalidGuestIds(command, activeGuests)
            if (invalid.isNotEmpty()) return UpdateInvitationResult.InvalidGuests(invalid)

            val alreadyAssigned = alreadyAssignedGuestIds(command, currentInvitation)
            if (alreadyAssigned.isNotEmpty()) return UpdateInvitationResult.AlreadyAssignedGuests(alreadyAssigned)

            return updateInvitation(command, currentInvitation, activeGuests)
        } ?: UpdateInvitationResult.NotFound

    private fun updateInvitation(
        command: UpdateInvitationCommand,
        currentInvitation: Invitation,
        activeGuests: Set<Guest>,
    ): UpdateInvitationResult =
        try {
            invitations.update(command.toInvitation(currentInvitation, activeGuests))
                ?.let { UpdateInvitationResult.Updated(it) }
                ?: UpdateInvitationResult.NotFound
        } catch (exception: DataIntegrityViolationException) {
            alreadyAssignedGuestIds(command, currentInvitation)
                .takeIf { it.isNotEmpty() }
                ?.let { UpdateInvitationResult.AlreadyAssignedGuests(it) }
                ?: throw exception
        }

    private fun alreadyAssignedGuestIds(command: UpdateInvitationCommand, currentInvitation: Invitation): Set<GuestId> =
        invitations.findAssignedGuestIds(command.guestIds) - currentInvitation.guests.map { it.id }.toSet()

    private fun invalidGuestIds(command: UpdateInvitationCommand, activeGuests: Set<Guest>) =
        command.guestIds - activeGuests.map { it.id }.toSet()
}
