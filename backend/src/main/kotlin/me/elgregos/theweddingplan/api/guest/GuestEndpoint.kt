package me.elgregos.theweddingplan.api.guest

import me.elgregos.theweddingplan.application.guest.GuestAdder
import me.elgregos.theweddingplan.application.guest.GuestLister
import me.elgregos.theweddingplan.domain.guest.Guest
import me.elgregos.theweddingplan.domain.guest.GuestPage
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.servlet.function.ServerRequest
import org.springframework.web.servlet.function.ServerResponse

@Component
class GuestEndpoint(
    private val guestAdder: GuestAdder,
    private val guestLister: GuestLister,
) {

    fun listGuests(request: ServerRequest): ServerResponse {
        val page = request.intQueryParam("page", 0) ?: return ServerResponse.badRequest().build()
        val size = request.intQueryParam("size", 20) ?: return ServerResponse.badRequest().build()

        if (page < 0 || size <= 0) {
            return ServerResponse.badRequest().build()
        }

        return guestLister.list(page = page, size = size)
            .toResponse()
            .let(ServerResponse.ok()::body)
    }

    fun addGuest(request: ServerRequest): ServerResponse {
        val addRequest = request.body(AddGuestRequest::class.java)
        return guestAdder.add(addRequest.firstName, addRequest.lastName, addRequest.email)
            .toResponse()
            .let { ServerResponse.status(HttpStatus.CREATED).body(it) }
    }
}

private fun ServerRequest.intQueryParam(name: String, default: Int): Int? {
    val value = param(name).orElse(null)?.trim()

    if (value.isNullOrEmpty()) {
        return default
    }

    return value.toIntOrNull()
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

data class GuestPageResponse(
    val items: List<GuestResponse>,
    val page: Int,
    val size: Int,
    val totalItems: Long,
    val totalPages: Int,
)

internal fun Guest.toResponse() =
    GuestResponse("$id", version, "$creationDate", "$updateDate", firstName, lastName, email)

internal fun GuestPage.toResponse() =
    GuestPageResponse(
        items = items.map(Guest::toResponse),
        page = page,
        size = size,
        totalItems = totalItems,
        totalPages = totalPages,
    )

