package me.elgregos.theweddingplan.application.invitation

import me.elgregos.theweddingplan.application.invitation.command.UpdateInvitationCommand
import me.elgregos.theweddingplan.application.invitation.result.UpdateInvitationResult
import me.elgregos.theweddingplan.domain.guest.entity.Guest
import me.elgregos.theweddingplan.domain.guest.entity.GuestId
import me.elgregos.theweddingplan.domain.guest.repository.Guests
import me.elgregos.theweddingplan.domain.invitation.entity.Invitation
import me.elgregos.theweddingplan.domain.invitation.repository.Invitations
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service

@Service
class InvitationUpdater(
    private val invitations: Invitations,
    private val guests: Guests,
) {

    fun update(command: UpdateInvitationCommand): UpdateInvitationResult =
        with(command) {
            invitations.findById(id)
                ?.let { existingInvitation ->
                    when {
                        existingInvitation.version != version -> UpdateInvitationResult.VersionConflict
                        guestIds.isEmpty() -> UpdateInvitationResult.MissingGuests
                        else -> guests.findByIds(guestIds)
                            .let { activeGuests ->
                                invalidGuestIds(this, activeGuests)
                                    .takeIf { it.isNotEmpty() }
                                    ?.let { UpdateInvitationResult.InvalidGuests(it) }
                                    ?: alreadyAssignedGuestIds(command, existingInvitation)
                                        .takeIf { it.isNotEmpty() }
                                        ?.let { UpdateInvitationResult.AlreadyAssignedGuests(it) }
                                    ?: updateInvitation(command, existingInvitation, activeGuests)
                            }
                    }
                } ?: UpdateInvitationResult.NotFound
        }

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
