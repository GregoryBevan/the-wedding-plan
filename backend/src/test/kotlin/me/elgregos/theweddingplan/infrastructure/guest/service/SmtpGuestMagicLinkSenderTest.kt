package me.elgregos.theweddingplan.infrastructure.guest.service

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import jakarta.mail.Message
import jakarta.mail.Session
import jakarta.mail.internet.MimeMessage
import jakarta.mail.internet.MimeMultipart
import me.elgregos.theweddingplan.domain.guest.entity.GuestFixtures.janeDoe
import me.elgregos.theweddingplan.domain.guest.entity.GuestMagicLinkFixtures.bridesMaidToJane
import me.elgregos.theweddingplan.infrastructure.config.GuestAccessPropertiesFixtures.testGuestAccessProperties
import me.elgregos.theweddingplan.infrastructure.config.MailProperties
import org.springframework.mail.MailSendException
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.context.support.ResourceBundleMessageSource
import java.util.*
import kotlin.test.BeforeTest
import kotlin.test.Test

class SmtpGuestMagicLinkSenderTest {

    private lateinit var javaMailSender: JavaMailSender
    private lateinit var smtpGuestMagicLinkSender: SmtpGuestMagicLinkSender

    @BeforeTest
    fun setUp() {
        javaMailSender = mockk(relaxed = true)
        val messageSource = ResourceBundleMessageSource().apply {
            setBasename("messages")
            setDefaultEncoding("UTF-8")
            setFallbackToSystemLocale(false)
        }
        smtpGuestMagicLinkSender = SmtpGuestMagicLinkSender(
            javaMailSender = javaMailSender,
            guestAccessProperties = testGuestAccessProperties.copy(baseUrl = "https://public.theweddingplan.app"),
            mailProperties = MailProperties(from = "no-reply@theweddingplan.app"),
            guestMagicLinkEmailTemplate = GuestMagicLinkEmailTemplate(messageSource),
        )
    }

    @Test
    fun `should send magic-link email with expected recipient and content`() {
        val messageSlot = slot<MimeMessage>()
        every { javaMailSender.createMimeMessage() } returns MimeMessage(Session.getInstance(Properties()))
        every { javaMailSender.send(capture(messageSlot)) } returns Unit

        smtpGuestMagicLinkSender.send(bridesMaidToJane, janeDoe)

        val sentMessage = messageSlot.captured
        val bodyContent = flattenMimeContent(sentMessage.content)

        verify(exactly = 1) { javaMailSender.send(any<MimeMessage>()) }
        assertThat(sentMessage.from.map { it.toString() }).isEqualTo(listOf("no-reply@theweddingplan.app"))
        assertThat(sentMessage.getRecipients(Message.RecipientType.TO).map { it.toString() })
            .isEqualTo(listOf(janeDoe.email))
        assertThat(sentMessage.subject).isEqualTo("Thecla & Grégory - Votre invitation")
        assertThat(bodyContent)
            .contains("Bonjour Jane")
        assertThat(bodyContent)
            .contains("https://public.theweddingplan.app/api/guest-access/magic-links/53c2efcd-b4fc-42f3-a73b-fadf3725af3f")
        assertThat(bodyContent)
            .contains("Accéder à mon invitation")
    }

    @Test
    fun `should swallow mail send exception`() {
        every { javaMailSender.createMimeMessage() } returns MimeMessage(Session.getInstance(Properties()))
        every { javaMailSender.send(any<MimeMessage>()) } throws MailSendException("smtp down")

        smtpGuestMagicLinkSender.send(bridesMaidToJane, janeDoe)

        verify(exactly = 1) { javaMailSender.send(any<MimeMessage>()) }
    }

    private fun flattenMimeContent(content: Any): String = when (content) {
        is String -> content
        is MimeMultipart -> (0 until content.count)
            .joinToString("\n") { index -> flattenMimeContent(content.getBodyPart(index).content) }
        else -> content.toString()
    }
}
