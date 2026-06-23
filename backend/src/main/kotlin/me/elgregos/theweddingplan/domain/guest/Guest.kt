package me.elgregos.theweddingplan.domain.guest

import me.elgregos.theweddingplan.domain.shared.Dates
import java.time.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
data class Guest(
    val id: GuestId = GuestId.generate(),
    val version: Long = 1L,
    val creationDate: LocalDateTime = Dates.nowUtcMillis(),
    val updateDate: LocalDateTime = Dates.nowUtcMillis(),
    val firstName: String,
    val lastName: String,
    val email: String,
)

