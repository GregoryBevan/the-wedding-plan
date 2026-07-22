package me.elgregos.theweddingplan.infrastructure.guest.service

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import jakarta.mail.Message
import jakarta.mail.Session
import jakarta.mail.internet.MimeMultipart
import jakarta.mail.internet.MimeMessage
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import me.elgregos.theweddingplan.domain.guest.entity.GuestMagicLinkFixtures.bridesMaidToJane
import me.elgregos.theweddingplan.infrastructure.config.GuestAccessProperties
import me.elgregos.theweddingplan.infrastructure.config.MailProperties
import org.springframework.mail.MailSendException
import org.springframework.mail.javamail.JavaMailSender
import java.util.Properties
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
            guestMagicLinkEmailTemplate = GuestMagicLinkEmailTemplate(),
        )
    }

    @Test
    fun `should send magic-link email with expected recipient and content`() {
        val messageSlot = slot<MimeMessage>()
        every { javaMailSender.createMimeMessage() } returns MimeMessage(Session.getInstance(Properties()))
        every { javaMailSender.send(capture(messageSlot)) } returns Unit

        smtpGuestMagicLinkSender.send(bridesMaidToJane)

        val sentMessage = messageSlot.captured
        val bodyContent = flattenMimeContent(sentMessage.content)

        verify(exactly = 1) { javaMailSender.send(any<MimeMessage>()) }
        assertThat(sentMessage.from.map { it.toString() }).isEqualTo(listOf("no-reply@theweddingplan.app"))
        assertThat(sentMessage.getRecipients(Message.RecipientType.TO).map { it.toString() })
            .isEqualTo(listOf(bridesMaidToJane.guestEmail))
        assertThat(sentMessage.subject).isEqualTo("Thecla & Grégory - Votre invitation")
        assertThat(bodyContent)
            .contains("Bonjour ${bridesMaidToJane.guestFirstName}")
        assertThat(bodyContent)
            .contains("https://public.theweddingplan.app/guest-access/${bridesMaidToJane.invitationAccessToken.value}/guests/${bridesMaidToJane.guestId}")
        assertThat(bodyContent)
            .contains("Accéder à mon invitation")
    }

    @Test
    fun `should swallow mail send exception`() {
        every { javaMailSender.createMimeMessage() } returns MimeMessage(Session.getInstance(Properties()))
        every { javaMailSender.send(any<MimeMessage>()) } throws MailSendException("smtp down")

        smtpGuestMagicLinkSender.send(bridesMaidToJane)

        verify(exactly = 1) { javaMailSender.send(any<MimeMessage>()) }
    }

    private fun flattenMimeContent(content: Any): String = when (content) {
        is String -> content
        is MimeMultipart -> (0 until content.count)
            .joinToString("\n") { index -> flattenMimeContent(content.getBodyPart(index).content) }
        else -> content.toString()
    }
}
