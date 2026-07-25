package me.elgregos.theweddingplan.infrastructure.config

object GuestAccessPropertiesFixtures {

    val testGuestAccessProperties = GuestAccessProperties(
        baseUrl = "http://localhost:8080",
        guestAreaUrl = "http://localhost:5174/guest-access/secured-area",
        jwtSecret = "cest-un-secret-de-test-qui-a-une-longueur-suffisante-pour-le-jwt",
    )

}