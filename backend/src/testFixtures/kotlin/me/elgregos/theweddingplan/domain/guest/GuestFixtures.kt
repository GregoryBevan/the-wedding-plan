package me.elgregos.theweddingplan.domain.guest

import java.time.LocalDateTime

object GuestFixtures {
    val creationDate: LocalDateTime = LocalDateTime.of(2026, 6, 13, 10, 0, 0)

    val johnDoe = Guest(
        id = GuestId.fromString("019e82e0-0cc1-727e-a483-947fea529ef3"),
        version = 1L,
        creationDate = creationDate,
        updateDate = creationDate,
        firstName = "John",
        lastName = "Doe",
        email = "john.doe@example.com"
    )

    val johnDoeUpdated = Guest(
        id = GuestId.fromString("019e82e0-0cc1-727e-a483-947fea529ef3"),
        version = 2L,
        firstName = "Johnny",
        lastName = "Doe",
        email = "johnny.doe@example.com",
        creationDate = creationDate,
        updateDate = creationDate.plusDays(1),
    )

    val johnDoeArchived = Guest(
        id = GuestId.fromString("019e82e0-0cc1-727e-a483-947fea529ef3"),
        version = 2L,
        firstName = "John",
        lastName = "Doe",
        email = "john.doe@example.com",
        creationDate = creationDate,
        updateDate = creationDate.plusDays(2),
        deletionDate = creationDate.plusDays(2),
    )

    val johnDoeRestored = Guest(
        id = GuestId.fromString("019e82e0-0cc1-727e-a483-947fea529ef3"),
        version = 3L,
        firstName = "John",
        lastName = "Doe",
        email = "john.doe@example.com",
        creationDate = creationDate,
        updateDate = creationDate.plusDays(3),
        deletionDate = null,
    )

    val janeDoe = Guest(
        id = GuestId.fromString("019e8807-5321-75ed-8a40-99a73529e50f"),
        version = 1L,
        creationDate = creationDate,
        updateDate = creationDate,
        firstName = "Jane",
        lastName = "Doe",
        email = "jane.doe@example.com"
    )

    val emmaWilson = Guest(
        id = GuestId.fromString("019f0e86-ccad-7cd2-b419-81c40b3dda3e"),
        version = 2L,
        firstName = "Emma",
        lastName = "Wilson",
        creationDate = creationDate,
        updateDate = creationDate.plusDays(1),
        email = "emma.wilson@example.com"
    )

    val liamMiller = Guest(
        id = GuestId.fromString("019f0e7e-8f4d-790b-af81-9af8f2db8501"),
        firstName = "Liam",
        lastName = "Miller",
        email = "liam.miller@example.com",
        creationDate = creationDate.plusDays(3),
        updateDate = creationDate.plusDays(3),
    )

    val liamMillerUpdated = Guest(
        id = GuestId.fromString("019f0e7e-8f4d-790b-af81-9af8f2db8501"),
        version = 2L,
        firstName = "Liam",
        lastName = "Miller",
        creationDate = creationDate.plusDays(3),
        updateDate = creationDate.plusDays(4),
        email = "liam.miller-bis@example.com"
    )

    val noahAnderson = Guest(
        id = GuestId.fromString("019f2285-30c1-7323-9fc1-69f6cc44b487"),
        firstName = "Noah",
        lastName = "Anderson",
        email = "noah.anderson@example.com",
        creationDate = creationDate,
        updateDate = creationDate,
    )

    val noahAndersonUpdated = Guest(
        id = GuestId.fromString("019f2285-30c1-7323-9fc1-69f6cc44b487"),
        version = 2L,
        firstName = "Noah",
        lastName = "Anderson",
        email = "noah.anderson@example.com",
        creationDate = creationDate,
        updateDate = creationDate.plusDays(3)
    )

    val mickaelKael = Guest(
        id = GuestId.fromString("019f2287-efe1-748b-a7f6-e20962d062cf"),
        version = 1L,
        firstName = "Mickael",
        lastName = "Kael",
        email = "mickael.kael@example.com",
        creationDate = creationDate,
        updateDate = creationDate
    )
}
