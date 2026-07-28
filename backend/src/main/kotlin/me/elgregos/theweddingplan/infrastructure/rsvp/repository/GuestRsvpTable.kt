package me.elgregos.theweddingplan.infrastructure.rsvp.repository

import me.elgregos.theweddingplan.infrastructure.guest.repository.GuestTable
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.datetime

object GuestRsvpTable : Table("guest_rsvp") {
    val id = uuid("id")
    val guestId = reference("guest_id", GuestTable.id, onDelete = ReferenceOption.RESTRICT)
    val version = long("version")
    val creationDate = datetime("creation_date")
    val updateDate = datetime("update_date")
    val attendance = text("attendance")
    override val primaryKey = PrimaryKey(id)
}

