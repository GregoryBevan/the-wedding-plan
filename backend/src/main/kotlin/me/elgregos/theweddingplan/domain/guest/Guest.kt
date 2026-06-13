package me.elgregos.theweddingplan.domain.guest

import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID
import com.github.f4b6a3.uuid.UuidCreator

data class Guest(
    val id: UUID = UuidCreator.getTimeOrderedEpoch(),
    val version: Long = 1L,
    val creationDate: LocalDateTime = LocalDateTime.now(ZoneOffset.UTC),
    val updateDate: LocalDateTime = LocalDateTime.now(ZoneOffset.UTC),
    val firstName: String,
    val lastName: String,
    val email: String,
)
