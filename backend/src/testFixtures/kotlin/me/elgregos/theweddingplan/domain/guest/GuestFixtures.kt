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

    val albertEinstein = Guest(
        id = GuestId.fromString("019f6c83-cd2d-7357-833b-e7cda750ba33"),
        version = 1L,
        firstName = "Albert",
        lastName = "Einstein",
        email = "albert@example.com",
        creationDate = creationDate,
        updateDate = creationDate,
    )

    val marieCurie = Guest(
        id = GuestId.fromString("019f6c83-cd2d-7357-833b-e7cda750ba34"),
        version = 1L,
        firstName = "Marie",
        lastName = "Curie",
        email = "marie@example.com",
        creationDate = creationDate,
        updateDate = creationDate,
    )

    val pierreCurie = Guest(
        id = GuestId.fromString("019f6c83-cd2d-7357-833b-e7cda750ba35"),
        version = 1L,
        firstName = "Pierre",
        lastName = "Curie",
        email = "pierre@example.com",
        creationDate = creationDate,
        updateDate = creationDate,
    )

    val ryanEvans = Guest(
        id = GuestId.fromString("019f7000-0000-7000-8000-000000000001"),
        version = 1L,
        firstName = "Ryan",
        lastName = "Evans",
        email = "ryanevans@teleworm.us",
        creationDate = creationDate,
        updateDate = creationDate,
    )

    val joyceClement = Guest(
        id = GuestId.fromString("019f7000-0000-7000-8000-000000000002"),
        version = 1L,
        firstName = "Joyce",
        lastName = "Clement",
        email = "joyceclement@example.com",
        creationDate = creationDate,
        updateDate = creationDate,
    )

    val julianneWhitaker = Guest(
        id = GuestId.fromString("019f7000-0000-7000-8000-000000000003"),
        version = 1L,
        firstName = "Julianne",
        lastName = "Whitaker",
        email = "juliannewhitaker@jourrapide.com",
        creationDate = creationDate,
        updateDate = creationDate,
    )

    val restoreCandidate = Guest(
        id = GuestId.fromString("019f7000-0000-7000-8000-000000000004"),
        version = 1L,
        firstName = "Restore",
        lastName = "Candidate",
        email = "restore.candidate@example.com",
        creationDate = creationDate,
        updateDate = creationDate,
    )

    val sarahMills = Guest(
        id = GuestId.fromString("019f7000-0000-7000-8000-000000000005"),
        version = 1L,
        firstName = "Sarah",
        lastName = "Mills",
        email = "sarahmills@example.com",
        creationDate = creationDate,
        updateDate = creationDate,
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

    val martinLaval = Guest(
        id = GuestId.fromString("019f5c29-d4e0-749b-a719-8eb3788ecc80"),
        version = 1L,
        firstName = "Martin",
        lastName = "Laval",
        email = "martin.laval@example.com",
        creationDate = creationDate,
        updateDate = creationDate
    )

    val pierrePonce = Guest(
        id = GuestId.fromString("019f5c2b-3194-71af-9492-38d2a7509e60"),
        version = 1L,
        firstName = "Pierre",
        lastName = "Ponce",
        email = "pierre.ponce@example.com",
        creationDate = creationDate,
        updateDate = creationDate
    )
}
