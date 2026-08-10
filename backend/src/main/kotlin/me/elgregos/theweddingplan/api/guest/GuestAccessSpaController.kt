package me.elgregos.theweddingplan.api.guest

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

/**
 * Serves the public guest SPA (built to `/public/index.html`) for its client-side routes, so a hard
 * navigation resolves to the app shell instead of a static-resource 404. Covers the guest landing at
 * `/`, the invitation landing `/guest-access/{token}` (QR target), and `/guest-access/secured-area`
 * (where the magic-link verification redirects).
 *
 * Mirrors [me.elgregos.theweddingplan.api.backoffice.BackofficeSpaController], which does the same for
 * the backoffice SPA. The API endpoints under `/api` are unaffected — those paths never match here.
 */
@Controller
class GuestAccessSpaController {

    @GetMapping(
        value = [
            "/",
            "/guest-access/secured-area",
            "/guest-access/{token}",
        ]
    )
    fun index(): String = "forward:/public/index.html"
}
