package me.elgregos.theweddingplan.infrastructure.guest.service

import assertk.all
import assertk.assertThat
import assertk.assertions.contains
import me.elgregos.theweddingplan.domain.guest.entity.GuestFixtures.janeDoe
import me.elgregos.theweddingplan.domain.guest.entity.GuestFixtures.oliverBennett
import me.elgregos.theweddingplan.domain.guest.entity.GuestMagicLinkFixtures.bridesMaidToJane
import me.elgregos.theweddingplan.domain.guest.entity.Language
import org.springframework.context.support.ResourceBundleMessageSource
import kotlin.test.BeforeTest
import kotlin.test.Test

class GuestMagicLinkEmailTemplateTest {

    private lateinit var guestMagicLinkEmailTemplate: GuestMagicLinkEmailTemplate

    @BeforeTest
    fun setUp() {
        val messageSource = ResourceBundleMessageSource().apply {
            setBasename("messages")
            setDefaultEncoding("UTF-8")
            setFallbackToSystemLocale(false)
        }
        guestMagicLinkEmailTemplate = GuestMagicLinkEmailTemplate(messageSource)
    }

    @Test
    fun `should render a user friendly text body`() {
        val magicLinkUrl = "https://public.theweddingplan.app${bridesMaidToJane.guestAccessPath()}"

        val textBody = guestMagicLinkEmailTemplate.textBody(janeDoe.firstName, magicLinkUrl, Language.FR)

        assertThat(textBody).all {
            contains("Bonjour ${janeDoe.firstName}")
            contains("lien sécurisé")
            contains("heureux de vous inviter")
            contains(magicLinkUrl)
        }
    }

    @Test
    fun `should render html body with cta and fallback link`() {
        val magicLinkUrl = "https://public.theweddingplan.app${bridesMaidToJane.guestAccessPath()}"

        val htmlBody = guestMagicLinkEmailTemplate.htmlBody(janeDoe.firstName, magicLinkUrl, Language.FR)

        assertThat(htmlBody).all {
            contains("<html lang=\"fr\">")
            contains("Thecla & Grégory")
            contains("Bonjour ${janeDoe.firstName}")
            contains("Accéder à mon invitation")
            contains(magicLinkUrl)
        }

    }

    @Test
    fun `should render an english text body when guest language is EN`() {
        val magicLinkUrl = "https://public.theweddingplan.app${bridesMaidToJane.guestAccessPath()}"

        val textBody = guestMagicLinkEmailTemplate.textBody(oliverBennett.firstName, magicLinkUrl, Language.EN)

        assertThat(textBody).all {
            contains("Hello ${oliverBennett.firstName}")
            contains("secure link")
            contains(magicLinkUrl)
        }
    }

    @Test
    fun `should render an english html body when guest language is EN`() {
        val magicLinkUrl = "https://public.theweddingplan.app${bridesMaidToJane.guestAccessPath()}"

        val htmlBody = guestMagicLinkEmailTemplate.htmlBody(oliverBennett.firstName, magicLinkUrl, Language.EN)

        assertThat(htmlBody).all {
            contains("<html lang=\"en\">")
            contains("Hello ${oliverBennett.firstName}")
            contains("Access my invitation")
            contains(magicLinkUrl)
        }
    }
}

