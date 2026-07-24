package me.elgregos.theweddingplan.infrastructure.guest.repository

import me.elgregos.theweddingplan.infrastructure.invitation.repository.InvitationTable
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.datetime

object GuestMagicLinkTokenTable : Table("guest_magic_link_token") {
    val id = uuid("id")
    val token = text("token")
    val invitationId = reference("invitation_id", InvitationTable.id, onDelete = ReferenceOption.CASCADE)
    val guestId = reference("guest_id", GuestTable.id, onDelete = ReferenceOption.RESTRICT)
    val creationDate = datetime("creation_date")
    val expiresAt = datetime("expires_at")
    val usedAt = datetime("used_at").nullable()

    override val primaryKey = PrimaryKey(id)
}

