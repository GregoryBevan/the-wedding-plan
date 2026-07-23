package me.elgregos.theweddingplan.api.common

import me.elgregos.theweddingplan.domain.guest.entity.GuestStatus
import me.elgregos.theweddingplan.domain.guest.entity.GuestAvailability
import me.elgregos.theweddingplan.domain.guest.entity.GuestId
import me.elgregos.theweddingplan.domain.guest.entity.GuestMagicLinkAccessToken
import me.elgregos.theweddingplan.domain.invitation.entity.InvitationAccessToken
import me.elgregos.theweddingplan.domain.invitation.entity.InvitationId
import org.springframework.web.servlet.function.ServerRequest

private const val STATUS_PARAM_NAME = "status"
private const val AVAILABILITY_PARAM_NAME = "availability"

internal fun ServerRequest.intQueryParam(name: String, default: Int): Int? =
    queryParamOrNull(name)?.toIntOrNull() ?: if (param(name).isEmpty) default else null

internal fun ServerRequest.statusQueryParam(): GuestStatus? =
    queryParamOrNull(STATUS_PARAM_NAME)?.toGuestStatus() ?: if (param(STATUS_PARAM_NAME).isEmpty) GuestStatus.ACTIVE else null

internal fun ServerRequest.availabilityQueryParam(): GuestAvailability? =
    queryParamOrNull(AVAILABILITY_PARAM_NAME)?.toGuestAvailability()
        ?: if (param(AVAILABILITY_PARAM_NAME).isEmpty) GuestAvailability.ALL else null

internal fun ServerRequest.guestIdPathParam(name: String = "id"): GuestId? =
    GuestId.fromStringOrNull(pathVariable(name))

internal fun ServerRequest.invitationIdPathParam(): InvitationId? =
    InvitationId.fromStringOrNull(pathVariable("id"))

internal fun ServerRequest.invitationAccessTokenPathParam() =
    InvitationAccessToken.fromStringOrNull(pathVariable("token"))

internal fun ServerRequest.magicLinkTokenPathParam() =
    GuestMagicLinkAccessToken.fromStringOrNull(pathVariable("token"))

internal fun ServerRequest.clientAddress() =
    remoteAddress()
        .map { it.address?.hostAddress ?: it.hostString }
        .orElseGet { servletRequest().remoteAddr ?: "unknown" }

private fun ServerRequest.queryParamOrNull(name: String) =
    param(name).orElse(null)

private fun String.toGuestStatus() =
    GuestStatus.entries.firstOrNull { it.name.equals(this, ignoreCase = true) }

private fun String.toGuestAvailability() =
    GuestAvailability.entries.firstOrNull { it.name.equals(this, ignoreCase = true) }
