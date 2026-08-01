package me.elgregos.theweddingplan.api.rsvp.response

object GuestRsvpResponseFixtures {
    val johnDoe = GuestRsvpResponse(
        id = "019fb445-4209-75ad-9370-e16ff6140b37",
        version = 1L,
        creationDate = "2026-06-13T10:00",
        updateDate = "2026-06-13T10:00",
        attendance = "ATTENDING",
    )

    val johnDoeWithChoices = GuestRsvpResponse(
        id = "019fb445-4209-75ad-9370-e16ff6140b37",
        version = 1L,
        creationDate = "2026-06-13T10:00",
        updateDate = "2026-06-13T10:00",
        attendance = "ATTENDING",
        meal = "MEAT",
        song = SongResponseFixtures.laVieEnRose,
    )

    val johnDoeMealOnly = GuestRsvpResponse(
        id = "019fb445-4209-75ad-9370-e16ff6140b37",
        version = 1L,
        creationDate = "2026-06-13T10:00",
        updateDate = "2026-06-13T10:00",
        attendance = "ATTENDING",
        meal = "VEGGIE",
        song = null,
    )
}

