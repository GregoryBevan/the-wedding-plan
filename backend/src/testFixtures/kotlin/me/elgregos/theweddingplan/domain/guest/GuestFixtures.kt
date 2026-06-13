package me.elgregos.theweddingplan.domain.guest

import java.time.LocalDateTime
import java.util.UUID

object GuestFixtures {
    val fixedDate: LocalDateTime = LocalDateTime.of(2026, 6, 13, 10, 0, 0)
    
    fun guest(
        firstName: String = "John",
        lastName: String = "Doe",
        email: String = "john.doe@example.com"
    ) = Guest(
        firstName = firstName,
        lastName = lastName,
        email = email
    )
    val johnDoe = Guest(
        id = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"),
        version = 1L,
        creationDate = fixedDate,
        updateDate = fixedDate,
        firstName = "John",
        lastName = "Doe",
        email = "john.doe@example.com"
    )

    val janeDoe = Guest(
        id = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12"),
        version = 1L,
        creationDate = fixedDate,
        updateDate = fixedDate,
        firstName = "Jane",
        lastName = "Doe",
        email = "jane.doe@example.com"
    )
}
