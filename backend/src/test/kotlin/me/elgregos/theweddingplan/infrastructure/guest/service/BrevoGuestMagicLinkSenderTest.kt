package me.elgregos.theweddingplan.infrastructure.guest.service

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import me.elgregos.theweddingplan.domain.guest.entity.GuestFixtures.janeDoe
import me.elgregos.theweddingplan.domain.guest.entity.GuestMagicLinkFixtures.bridesMaidToJane
import me.elgregos.theweddingplan.infrastructure.config.BrevoProperties
import me.elgregos.theweddingplan.infrastructure.config.GuestAccessPropertiesFixtures.testGuestAccessProperties
import me.elgregos.theweddingplan.infrastructure.config.MailProperties
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.web.client.RestClientException
import kotlin.test.BeforeTest
import kotlin.test.Test

class BrevoGuestMagicLinkSenderTest {

    private lateinit var brevoApi: BrevoApi
    private lateinit var brevoGuestMagicLinkSender: BrevoGuestMagicLinkSender

    @BeforeTest
    fun setUp() {
        brevoApi = mockk(relaxed = true)
        val messageSource = ResourceBundleMessageSource().apply {
            setBasename("messages")
            setDefaultEncoding("UTF-8")
            setFallbackToSystemLocale(false)
        }
        brevoGuestMagicLinkSender = BrevoGuestMagicLinkSender(
            brevoApi = brevoApi,
            guestAccessProperties = testGuestAccessProperties.copy(baseUrl = "https://public.theweddingplan.app"),
            mailProperties = MailProperties(from = "no-reply@theweddingplan.app", provider = "brevo"),
            brevoProperties = BrevoProperties(apiKey = "test-key", senderName = "Wedding Plan"),
            guestMagicLinkEmailTemplate = GuestMagicLinkEmailTemplate(messageSource),
        )
    }

    @Test
    fun `should send magic-link email with expected sender, recipient and content`() {
        val requestSlot = slot<BrevoSendEmailRequest>()
        every { brevoApi.sendTransactionalEmail(capture(requestSlot)) } returns Unit

        brevoGuestMagicLinkSender.send(bridesMaidToJane, janeDoe)

        val request = requestSlot.captured
        assertThat(request.sender.email).isEqualTo("no-reply@theweddingplan.app")
        assertThat(request.to.map { it.email }).isEqualTo(listOf(janeDoe.email))
        assertThat(request.subject).contains("Votre invitation")
        assertThat(request.textContent).contains("Bonjour Jane")
        assertThat(request.htmlContent)
            .contains("https://public.theweddingplan.app/api/guest-access/magic-links/53c2efcd-b4fc-42f3-a73b-fadf3725af3f")
    }

    @Test
    fun `should swallow a provider failure`() {
        every { brevoApi.sendTransactionalEmail(any()) } throws RestClientException("brevo down")

        brevoGuestMagicLinkSender.send(bridesMaidToJane, janeDoe)

        verify(exactly = 1) { brevoApi.sendTransactionalEmail(any()) }
    }
}

