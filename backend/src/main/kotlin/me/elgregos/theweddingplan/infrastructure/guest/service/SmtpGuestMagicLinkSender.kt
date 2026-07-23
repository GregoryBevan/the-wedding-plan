package me.elgregos.theweddingplan.infrastructure.guest.service

import io.github.oshai.kotlinlogging.KotlinLogging
import me.elgregos.theweddingplan.domain.guest.entity.Guest
import me.elgregos.theweddingplan.domain.guest.entity.GuestMagicLink
import me.elgregos.theweddingplan.domain.guest.service.GuestMagicLinkSender
import me.elgregos.theweddingplan.infrastructure.config.GuestAccessProperties
import me.elgregos.theweddingplan.infrastructure.config.MailProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.mail.MailException
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Component
import org.springframework.context.annotation.Primary

private val logger = KotlinLogging.logger {}

@Component
@Primary
@ConditionalOnProperty(prefix = "app.mail", name = ["enabled"], havingValue = "true", matchIfMissing = true)
class SmtpGuestMagicLinkSender(
    private val javaMailSender: JavaMailSender,
    private val guestAccessProperties: GuestAccessProperties,
    private val mailProperties: MailProperties,
    private val guestMagicLinkEmailTemplate: GuestMagicLinkEmailTemplate,
) : GuestMagicLinkSender {

    override fun send(guestMagicLink: GuestMagicLink, guest: Guest) {
        val baseUrl = guestAccessProperties.baseUrl.trim().removeSuffix("/")
        val magicLinkUrl = "$baseUrl${guestMagicLink.guestAccessPath()}"

        val message = javaMailSender.createMimeMessage()
        MimeMessageHelper(message, true, Charsets.UTF_8.name()).apply {
            setFrom(mailProperties.from)
            setTo(guest.email)
            setSubject(guestMagicLinkEmailTemplate.subject())
            setText(
                guestMagicLinkEmailTemplate.textBody(guestFirstName = guest.firstName, magicLinkUrl = magicLinkUrl),
                guestMagicLinkEmailTemplate.htmlBody(guestFirstName = guest.firstName, magicLinkUrl = magicLinkUrl),
            )
        }

        runCatching { javaMailSender.send(message) }
            .onFailure { error ->
                if (error is MailException) {
                    logger.warn {
                        "Failed to send magic-link email (invitationId=${guestMagicLink.invitationId}, guestId=${guestMagicLink.guestId})"
                    }
                    return
                }
                throw error
            }
    }

}
