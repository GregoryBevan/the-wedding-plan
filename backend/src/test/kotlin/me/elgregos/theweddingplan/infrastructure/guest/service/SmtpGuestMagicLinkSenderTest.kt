package me.elgregos.theweddingplan.infrastructure.guest.service

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import me.elgregos.theweddingplan.domain.guest.entity.GuestMagicLinkFixtures.bridesMaidToJane
import me.elgregos.theweddingplan.infrastructure.config.GuestAccessProperties
import me.elgregos.theweddingplan.infrastructure.config.MailProperties
import org.springframework.mail.MailSendException
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import kotlin.test.BeforeTest
import kotlin.test.Test

class SmtpGuestMagicLinkSenderTest {

    private lateinit var javaMailSender: JavaMailSender
    private lateinit var smtpGuestMagicLinkSender: SmtpGuestMagicLinkSender

    @BeforeTest
    fun setUp() {
        javaMailSender = mockk(relaxed = true)
        smtpGuestMagicLinkSender = SmtpGuestMagicLinkSender(
            javaMailSender = javaMailSender,
            guestAccessProperties = GuestAccessProperties(baseUrl = "https://public.theweddingplan.app"),
            mailProperties = MailProperties(from = "no-reply@theweddingplan.app"),
        )
    }

    @Test
    fun `should send magic-link email with expected recipient and content`() {
        val messageSlot = slot<SimpleMailMessage>()
        every { javaMailSender.send(capture(messageSlot)) } returns Unit

        smtpGuestMagicLinkSender.send(bridesMaidToJane)

        verify(exactly = 1) { javaMailSender.send(any<SimpleMailMessage>()) }
        assertThat(messageSlot.captured.from).isEqualTo("no-reply@theweddingplan.app")
        assertThat(messageSlot.captured.to?.toList()).isEqualTo(listOf(bridesMaidToJane.guestEmail))
        assertThat(messageSlot.captured.subject).isEqualTo("Your wedding magic link")
        assertThat(messageSlot.captured.text ?: "").contains(bridesMaidToJane.invitationAccessToken.value)
        assertThat(messageSlot.captured.text ?: "").contains(bridesMaidToJane.guestId.toString())
    }

    @Test
    fun `should swallow mail send exception`() {
        every { javaMailSender.send(any<SimpleMailMessage>()) } throws MailSendException("smtp down")

        smtpGuestMagicLinkSender.send(bridesMaidToJane)

        verify(exactly = 1) { javaMailSender.send(any<SimpleMailMessage>()) }
    }
}
