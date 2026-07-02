package me.elgregos.theweddingplan.api.common

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.mockk.every
import io.mockk.mockk
import me.elgregos.theweddingplan.domain.guest.GuestStatus
import me.elgregos.theweddingplan.domain.guest.GuestId
import me.elgregos.theweddingplan.domain.invitation.InvitationId
import org.springframework.web.servlet.function.ServerRequest
import java.util.Optional
import kotlin.test.Test

class ServerRequestExtensionsTest {

    @Test
    fun `should return default int query param when value is missing`() {
        val request = mockk<ServerRequest>()

        every { request.param("page") } returns Optional.empty()

        assertThat(request.intQueryParam("page", default = 0)).isEqualTo(0)
    }

    @Test
    fun `should return null when int query param is not numeric`() {
        val request = mockk<ServerRequest>()

        every { request.param("size") } returns Optional.of("abc")

        assertThat(request.intQueryParam("size", default = 20)).isEqualTo(null)
    }

    @Test
    fun `should return null when int query param has surrounding spaces`() {
        val request = mockk<ServerRequest>()

        every { request.param("size") } returns Optional.of(" 20 ")

        assertThat(request.intQueryParam("size", default = 20)).isEqualTo(null)
    }

    @Test
    fun `should return status archived when status is archived`() {
        val request = mockk<ServerRequest>()

        every { request.param("status") } returns Optional.of("ARCHIVED")

        assertThat(request.statusQueryParam()).isEqualTo(GuestStatus.ARCHIVED)
    }

    @Test
    fun `should return null when explicit status is invalid`() {
        val request = mockk<ServerRequest>()

        every { request.param("status") } returns Optional.of("unknown")

        assertThat(request.statusQueryParam()).isEqualTo(null)
    }

    @Test
    fun `should default status to active when status query param is missing`() {
        val request = mockk<ServerRequest>()

        every { request.param("status") } returns Optional.empty()

        assertThat(request.statusQueryParam()).isEqualTo(GuestStatus.ACTIVE)
    }

    @Test
    fun `should parse guest id path param`() {
        val request = mockk<ServerRequest>()
        val guestId = GuestId.fromString("019f70eb-f060-7d9f-8dd8-f9caeca9d078")

        every { request.pathVariable("id") } returns guestId.toString()

        assertThat(request.guestIdPathParam()).isEqualTo(guestId)
    }

    @Test
    fun `should return null when guest id path param has surrounding spaces`() {
        val request = mockk<ServerRequest>()

        every { request.pathVariable("id") } returns " 019f70eb-f060-7d9f-8dd8-f9caeca9d078 "

        assertThat(request.guestIdPathParam()).isEqualTo(null)
    }

    @Test
    fun `should parse invitation id path param`() {
        val request = mockk<ServerRequest>()
        val invitationId = InvitationId.fromString("019f2282-7971-77e6-8d25-7568739fca0f")

        every { request.pathVariable("id") } returns invitationId.toString()

        assertThat(request.invitationIdPathParam()).isEqualTo(invitationId)
    }
}

