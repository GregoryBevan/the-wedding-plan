package me.elgregos.theweddingplan.domain.guest

import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
data class Guest(
    val id: GuestId = GuestId.generate(),
    val version: Long = 1L,
    val creationDate: LocalDateTime = LocalDateTime.now(ZoneOffset.UTC),
    val updateDate: LocalDateTime = LocalDateTime.now(ZoneOffset.UTC),
    val firstName: String,
    val lastName: String,
    val email: String,
)
