package me.elgregos.theweddingplan.infrastructure.rsvp.repository

import me.elgregos.theweddingplan.domain.rsvp.entity.RsvpAnswers
import me.elgregos.theweddingplan.infrastructure.shared.genericJsonMapper
import me.elgregos.theweddingplan.infrastructure.guest.repository.GuestTable
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.datetime
import org.jetbrains.exposed.v1.json.jsonb

object GuestRsvpTable : Table("guest_rsvp") {
    val id = uuid("id")
    val guestId = reference("guest_id", GuestTable.id, onDelete = ReferenceOption.RESTRICT)
    val version = long("version")
    val creationDate = datetime("creation_date")
    val updateDate = datetime("update_date")
    val attendance = text("attendance")
    val answers = jsonb("answers", { it.toJson() }, { RsvpAnswers.fromString(it) }).nullable()
    override val primaryKey = PrimaryKey(id)
}

fun RsvpAnswers.toJson(): String = genericJsonMapper.writeValueAsString(this)

fun RsvpAnswers.Companion.fromString(value: String): RsvpAnswers = genericJsonMapper.readValue(value, RsvpAnswers::class.java)

