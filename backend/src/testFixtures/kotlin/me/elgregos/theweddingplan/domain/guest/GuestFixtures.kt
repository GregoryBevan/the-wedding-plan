package me.elgregos.theweddingplan.domain.guest

import java.time.LocalDateTime

object GuestFixtures {
    val fixedDate: LocalDateTime = LocalDateTime.of(2026, 6, 13, 10, 0, 0)

    val johnDoe = Guest(
        id = GuestId.fromString("019f0e7e-8f4d-7378-a18d-d9ce21a7dd15"),
        version = 1L,
        creationDate = fixedDate,
        updateDate = fixedDate,
        firstName = "John",
        lastName = "Doe",
        email = "john.doe@example.com"
    )

    val johnDoeUpdated = Guest(
        id = GuestId.fromString("019f0e7e-8f4d-7378-a18d-d9ce21a7dd15"),
        version = 2L,
        firstName = "Johnny",
        lastName = "Doe",
        email = "johnny.doe@example.com",
        creationDate = fixedDate,
        updateDate = fixedDate.plusDays(1),
    )

    val johnDoeDeleted = Guest(
        id = GuestId.fromString("019f0e7e-8f4d-7378-a18d-d9ce21a7dd15"),
        version = 2L,
        firstName = "John",
        lastName = "Doe",
        email = "john.doe@example.com",
        creationDate = fixedDate,
        updateDate = fixedDate.plusDays(2),
        deletionDate = fixedDate.plusDays(2),
    )

    val johnDoeRestored = Guest(
        id = GuestId.fromString("019f0e7e-8f4d-7378-a18d-d9ce21a7dd15"),
        version = 3L,
        firstName = "John",
        lastName = "Doe",
        email = "john.doe@example.com",
        creationDate = fixedDate,
        updateDate = fixedDate.plusDays(3),
        deletionDate = null,
    )

    val janeDoe = Guest(
        id = GuestId.fromString("019f0e7e-8f4d-7efd-a833-3fd432e4d5c0"),
        version = 1L,
        creationDate = fixedDate,
        updateDate = fixedDate,
        firstName = "Jane",
        lastName = "Doe",
        email = "jane.doe@example.com"
    )

    val emmaWilson = Guest(
        id = GuestId.fromString("019f0e86-ccad-7cd2-b419-81c40b3dda3e"),
        version = 2L,
        firstName = "Emma",
        lastName = "Wilson",
        creationDate = fixedDate,
        updateDate = fixedDate.plusDays(1),
        email = "emma.wilson@example.com"
    )

    val liamMiller = Guest(
        id = GuestId.fromString("019f0e7e-8f4d-790b-af81-9af8f2db8501"),
        firstName = "Liam",
        lastName = "Miller",
        email = "liam.miller@example.com",
        creationDate = fixedDate.plusDays(3),
        updateDate = fixedDate.plusDays(3),
    )

    val liamMillerUpdated = Guest(
        id = GuestId.fromString("019f0e7e-8f4d-790b-af81-9af8f2db8501"),
        version = 2L,
        firstName = "Liam",
        lastName = "Miller",
        creationDate = fixedDate.plusDays(3),
        updateDate = fixedDate.plusDays(4),
        email = "liam.miller-bis@example.com"
    )

    val noahAnderson = Guest(
        id = GuestId.fromString("019f0e7e-8f4d-7f68-91a6-c23cdf606233"),
        firstName = "Noah",
        lastName = "Anderson",
        email = "noah.anderson@example.com",
        creationDate = fixedDate,
        updateDate = fixedDate,
    )

    val noahAndersonUpdated = Guest(
        id = GuestId.fromString("019f0e7e-8f4d-7f68-91a6-c23cdf606233"),
        version = 2L,
        firstName = "Noah",
        lastName = "Anderson",
        email = "noah.anderson@example.com",
        creationDate = fixedDate,
        updateDate = fixedDate.plusDays(3)
    )
}
