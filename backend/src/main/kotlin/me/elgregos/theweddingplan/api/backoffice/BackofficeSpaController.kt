package me.elgregos.theweddingplan.api.backoffice

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class BackofficeSpaController {

    @GetMapping(
        value = [
            "/backoffice",
            "/backoffice/",
            "/backoffice/invitations/**",
            "/backoffice/guests/**",
            "/backoffice/login-required",
            "/backoffice/access-denied",
        ]
    )
    fun index(): String = "forward:/backoffice/index.html"
}

