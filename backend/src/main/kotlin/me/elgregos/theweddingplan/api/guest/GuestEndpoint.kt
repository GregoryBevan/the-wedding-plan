package me.elgregos.theweddingplan.api.guest

import me.elgregos.theweddingplan.application.guest.GuestAdder
import me.elgregos.theweddingplan.domain.guest.Guest
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.servlet.function.ServerRequest
import org.springframework.web.servlet.function.ServerResponse

@Component
class GuestEndpoint(private val guestAdder: GuestAdder) {

    fun addGuest(request: ServerRequest): ServerResponse {
        val addRequest = request.body(AddGuestRequest::class.java)
        return guestAdder.add(addRequest.firstName, addRequest.lastName, addRequest.email)
            .toResponse()
            .let { ServerResponse.status(HttpStatus.CREATED).body(it) }
    }
}

data class AddGuestRequest(val firstName: String, val lastName: String, val email: String)

data class GuestResponse(
    val id: String,
    val version: Long,
    val creationDate: String,
    val updateDate: String,
    val firstName: String,
    val lastName: String,
    val email: String
)

internal fun Guest.toResponse() =
    GuestResponse("$id", version, "$creationDate", "$updateDate", firstName, lastName, email)
