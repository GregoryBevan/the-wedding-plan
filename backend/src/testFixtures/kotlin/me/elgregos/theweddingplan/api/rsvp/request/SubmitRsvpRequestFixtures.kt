package me.elgregos.theweddingplan.api.rsvp.request

object SubmitRsvpRequestFixtures {
    val attending = SubmitRsvpRequest(
        attendance = "ATTENDING",
        meal = "MEAT",
        song = SubmitSongRequestFixtures.laVieEnRose,
    )

    val attendingVeggie = SubmitRsvpRequest(
        attendance = "ATTENDING",
        meal = "VEGGIE",
    )

    val declined = SubmitRsvpRequest(
        attendance = "DECLINED",
        meal = "MEAT",
        song = SubmitSongRequestFixtures.laVieEnRose,
    )

    val invalidAttendance = SubmitRsvpRequest(
        attendance = "MAYBE",
        meal = "MEAT",
    )

    val attendingWithoutMeal = SubmitRsvpRequest(
        attendance = "ATTENDING",
    )

    val attendingUnknownMeal = SubmitRsvpRequest(
        attendance = "ATTENDING",
        meal = "PIZZA",
    )
}

