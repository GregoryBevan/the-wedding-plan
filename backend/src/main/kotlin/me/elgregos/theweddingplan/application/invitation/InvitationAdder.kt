package me.elgregos.theweddingplan.application.invitation

import me.elgregos.theweddingplan.domain.guest.Guest
import me.elgregos.theweddingplan.domain.guest.Guests
import me.elgregos.theweddingplan.domain.invitation.Invitations
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service

@Service
class InvitationAdder(
    private val invitations: Invitations,
    private val guests: Guests,
) {

    fun add(command: AddInvitationCommand): AddInvitationResult =
        command.guestIds
            .takeIf { it.isNotEmpty() }
            ?.let(guests::findByIds)
            ?.let { activeGuests ->
                invalidGuestIds(command, activeGuests)
                    .takeIf { it.isNotEmpty() }
                    ?.let { AddInvitationResult.InvalidGuests(it) }
                    ?: alreadyAssignedGuestIds(command)
                        .takeIf { it.isNotEmpty() }
                        ?.let { AddInvitationResult.AlreadyAssignedGuests(it) }
                        ?: addInvitation(command, activeGuests)
            }
            ?: AddInvitationResult.MissingGuests

    private fun addInvitation(command: AddInvitationCommand, activeGuests: Set<Guest>): AddInvitationResult =
        try {
            AddInvitationResult.Added(invitations.add(command.toInvitation(activeGuests)))
        } catch (exception: DataIntegrityViolationException) {
            alreadyAssignedGuestIds(command)
                .takeIf { it.isNotEmpty() }
                ?.let { AddInvitationResult.AlreadyAssignedGuests(it) }
                ?: throw exception
        }

    private fun alreadyAssignedGuestIds(command: AddInvitationCommand) =
        invitations.findAssignedGuestIds(command.guestIds)

    private fun invalidGuestIds(command: AddInvitationCommand, activeGuests: Set<Guest>) =
        command.guestIds - activeGuests.map { it.id }.toSet()

}

