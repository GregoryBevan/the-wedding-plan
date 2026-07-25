package me.elgregos.theweddingplan.api.guest

import org.springframework.stereotype.Component
import org.springframework.web.servlet.function.ServerRequest
import org.springframework.web.servlet.function.ServerResponse

@Component
class GuestRsvpEndpoint {

    // Placeholder guarded mutation endpoint (#109 covers authorization only).
    // Attendance, meal choice and song submission will be implemented in a later issue.
    fun submit(request: ServerRequest): ServerResponse =
        ServerResponse.noContent().build()
}

