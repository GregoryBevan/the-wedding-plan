package me.elgregos.theweddingplan.infrastructure.invitation

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.datetime

object InvitationTable : Table("invitation") {
    val id = uuid("id")
    val version = long("version")
    val creationDate = datetime("creation_date")
    val updateDate = datetime("update_date")
    val label = text("label")
    val description = text("description")
    override val primaryKey = PrimaryKey(id)
}

