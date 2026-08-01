package me.elgregos.theweddingplan.domain.rsvp.entity

import me.elgregos.theweddingplan.domain.guest.entity.GuestFixtures.johnDoe
import java.time.LocalDateTime

object GuestRsvpFixtures {
    val creationDate: LocalDateTime = LocalDateTime.of(2026, 6, 13, 10, 0, 0)

    val laVieEnRose = SongChoice(
        deezerId = 3135556L,
        title = "La Vie en rose",
        artist = "Édith Piaf",
        link = "https://www.deezer.com/track/3135556",
        preview = "https://cdns-preview.deezer.com/stream/la-vie-en-rose.mp3",
    )

    val johnDoeAnswers = RsvpAnswers(meal = Meal.MEAT, song = laVieEnRose)

    val veggieAnswers = RsvpAnswers(meal = Meal.VEGGIE)

    val johnDoeAnswersJson =
        """{"meal":"MEAT","song":{"deezerId":3135556,"title":"La Vie en rose","artist":"Édith Piaf","link":"https://www.deezer.com/track/3135556","preview":"https://cdns-preview.deezer.com/stream/la-vie-en-rose.mp3"}}"""

    val veggieAnswersJson =
        """{"meal":"VEGGIE","song":null}"""

    val johnDoeRsvp = GuestRsvp(
        id = GuestRsvpId.fromString("019fb445-4209-75ad-9370-e16ff6140b37"),
        guestId = johnDoe.id,
        version = 1L,
        creationDate = creationDate,
        updateDate = creationDate,
        attendance = RsvpAttendance.ATTENDING,
    )

    val johnDoeRsvpUpdated = johnDoeRsvp.copy(
        version = 2L,
        updateDate = creationDate.plusDays(1),
        attendance = RsvpAttendance.DECLINED,
    )

    val johnDoeRsvpWithChoices = johnDoeRsvp.copy(
        version = 2L,
        updateDate = creationDate.plusDays(1),
        answers = johnDoeAnswers,
    )

}


