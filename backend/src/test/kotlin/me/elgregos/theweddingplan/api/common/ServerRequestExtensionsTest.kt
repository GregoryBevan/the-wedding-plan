package me.elgregos.theweddingplan.api.common

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.mockk.every
import io.mockk.mockk
import me.elgregos.theweddingplan.domain.guest.entity.GuestStatus
import me.elgregos.theweddingplan.domain.guest.entity.GuestId
import me.elgregos.theweddingplan.domain.invitation.entity.InvitationId
import org.springframework.web.servlet.function.ServerRequest
import java.net.InetSocketAddress
import java.util.Optional
import jakarta.servlet.http.HttpServletRequest
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
    fun `should parse guest id from custom path variable name`() {
        val request = mockk<ServerRequest>()
        val guestId = GuestId.fromString("019f70eb-f060-7d9f-8dd8-f9caeca9d078")

        every { request.pathVariable("guestId") } returns guestId.toString()

        assertThat(request.guestIdPathParam("guestId")).isEqualTo(guestId)
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

    @Test
    fun `should resolve client address from server request remote address`() {
        val request = mockk<ServerRequest>()

        every { request.remoteAddress() } returns Optional.of(InetSocketAddress("203.0.113.11", 443))

        assertThat(request.clientAddress()).isEqualTo("203.0.113.11")
    }

    @Test
    fun `should fallback to servlet remote addr when server request remote address is missing`() {
        val request = mockk<ServerRequest>()
        val servletRequest = mockk<HttpServletRequest>()

        every { request.remoteAddress() } returns Optional.empty()
        every { request.servletRequest() } returns servletRequest
        every { servletRequest.remoteAddr } returns "198.51.100.77"

        assertThat(request.clientAddress()).isEqualTo("198.51.100.77")
    }

    @Test
    fun `should fallback to unknown when no address can be resolved`() {
        val request = mockk<ServerRequest>()
        val servletRequest = mockk<HttpServletRequest>()

        every { request.remoteAddress() } returns Optional.empty()
        every { request.servletRequest() } returns servletRequest
        every { servletRequest.remoteAddr } returns null

        assertThat(request.clientAddress()).isEqualTo("unknown")
    }
}
