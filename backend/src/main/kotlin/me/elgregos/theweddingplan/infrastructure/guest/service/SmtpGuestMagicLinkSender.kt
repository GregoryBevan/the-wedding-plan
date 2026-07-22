package me.elgregos.theweddingplan.infrastructure.guest.service

import io.github.oshai.kotlinlogging.KotlinLogging
import me.elgregos.theweddingplan.domain.guest.entity.GuestMagicLink
import me.elgregos.theweddingplan.domain.guest.service.GuestMagicLinkSender
import me.elgregos.theweddingplan.infrastructure.config.GuestAccessProperties
import me.elgregos.theweddingplan.infrastructure.config.MailProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.mail.MailException
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Component
import org.springframework.context.annotation.Primary

private val logger = KotlinLogging.logger {}

@Component
@Primary
@ConditionalOnBean(JavaMailSender::class)
@ConditionalOnProperty(prefix = "app.mail", name = ["enabled"], havingValue = "true", matchIfMissing = true)
class SmtpGuestMagicLinkSender(
    private val javaMailSender: JavaMailSender,
    private val guestAccessProperties: GuestAccessProperties,
    private val mailProperties: MailProperties,
) : GuestMagicLinkSender {

    override fun send(guestMagicLink: GuestMagicLink) {
        val baseUrl = guestAccessProperties.baseUrl.trim().removeSuffix("/")
        val magicLinkUrl = "$baseUrl${guestMagicLink.guestAccessPath()}"

        val message = SimpleMailMessage().apply {
            from = mailProperties.from
            setTo(guestMagicLink.guestEmail)
            subject = "Your wedding magic link"
            text = "Use this link to access your invitation: $magicLinkUrl"
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
