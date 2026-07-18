package me.elgregos.theweddingplan.infrastructure.guest.repository

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.datetime

object GuestTable : Table("guest") {
    val id = uuid("id")
    val version = long("version")
    val creationDate = datetime("creation_date")
    val updateDate = datetime("update_date")
    val deletionDate = datetime("deletion_date").nullable()
    val firstName = text("first_name")
    val lastName = text("last_name")
    val email = text("email")
    override val primaryKey = PrimaryKey(id)
}
