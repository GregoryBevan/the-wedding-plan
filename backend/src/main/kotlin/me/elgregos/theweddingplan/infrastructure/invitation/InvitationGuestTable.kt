package me.elgregos.theweddingplan.infrastructure.invitation

import me.elgregos.theweddingplan.infrastructure.guest.GuestTable
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table

object InvitationGuestTable : Table("invitation_guest") {
    val invitationId = reference("invitation_id", InvitationTable.id, onDelete = ReferenceOption.CASCADE)
    val guestId = reference("guest_id", GuestTable.id, onDelete = ReferenceOption.RESTRICT)
    override val primaryKey = PrimaryKey(invitationId, guestId)
}

