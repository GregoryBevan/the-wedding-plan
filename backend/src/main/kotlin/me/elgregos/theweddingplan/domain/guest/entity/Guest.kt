package me.elgregos.theweddingplan.domain.guest.entity

import me.elgregos.theweddingplan.domain.shared.Dates
import java.time.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
data class Guest(
    val id: GuestId = GuestId.generate(),
    val version: Long = 1L,
    val creationDate: LocalDateTime = Dates.nowUtcMillis(),
    val updateDate: LocalDateTime = Dates.nowUtcMillis(),
    val deletionDate: LocalDateTime? = null,
    val firstName: String,
    val lastName: String,
    val email: String,
) {
    fun updateDetails(
        firstName: String,
        lastName: String,
        email: String,
        now: LocalDateTime = Dates.nowUtcMillis(),
    ) = copy(
        version = version + 1,
        updateDate = now,
        firstName = firstName,
        lastName = lastName,
        email = email,
    )

    fun markAsArchived(now: LocalDateTime = Dates.nowUtcMillis()) =
        if (deletionDate != null) this
        else copy(
            version = version + 1,
            updateDate = now,
            deletionDate = now,
        )

    fun restore(now: LocalDateTime = Dates.nowUtcMillis()) =
        if (deletionDate == null) this
        else copy(
            version = version + 1,
            updateDate = now,
            deletionDate = null,
        )
}

