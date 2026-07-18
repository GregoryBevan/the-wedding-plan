package me.elgregos.theweddingplan.api.guest

import me.elgregos.theweddingplan.domain.guest.entity.GuestFixtures

object GuestResponseFixtures {
    val johnDoe = GuestResponse(
        id = "${GuestFixtures.johnDoe.id}",
        version = GuestFixtures.johnDoe.version,
        creationDate = "${GuestFixtures.creationDate}",
        updateDate = "${GuestFixtures.creationDate}",
        firstName = GuestFixtures.johnDoe.firstName,
        lastName = GuestFixtures.johnDoe.lastName,
        email = GuestFixtures.johnDoe.email
    )
}
