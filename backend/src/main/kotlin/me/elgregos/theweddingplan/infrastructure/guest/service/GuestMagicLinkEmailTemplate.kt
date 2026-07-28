package me.elgregos.theweddingplan.infrastructure.guest.service

import me.elgregos.theweddingplan.domain.guest.entity.Language
import org.springframework.context.MessageSource
import org.springframework.stereotype.Component
import org.springframework.web.util.HtmlUtils.htmlEscape
import java.util.Locale

@Component
class GuestMagicLinkEmailTemplate(
    private val messages: MessageSource,
) {

    fun subject(language: Language) =
        message("email.magic-link.subject", language.toLocale())

    fun textBody(guestFirstName: String, magicLinkUrl: String, language: Language): String {
        val locale = language.toLocale()
        return """
            ${message("email.magic-link.greeting", locale, guestFirstName)}

            ${message("email.magic-link.intro", locale)}
            $magicLinkUrl

            ${message("email.magic-link.ignore", locale)}

            --
            ${message("email.magic-link.signature", locale)}
        """.trimIndent()
    }

    fun htmlBody(guestFirstName: String, magicLinkUrl: String, language: Language): String {
        val locale = language.toLocale()
        return htmlTemplate(
            lang = locale.language,
            greeting = message("email.magic-link.greeting", locale, htmlEscape(guestFirstName)),
            intro = message("email.magic-link.intro", locale),
            cta = message("email.magic-link.cta", locale),
            fallback = message("email.magic-link.fallback", locale),
            ignore = message("email.magic-link.ignore", locale),
            signature = message("email.magic-link.signature", locale),
            magicLinkUrl = magicLinkUrl,
        )
    }

    private fun message(code: String, locale: Locale, vararg args: Any) =
        messages.getMessage(code, args.takeIf { it.isNotEmpty() }, locale)!!

    private fun htmlTemplate(
        lang: String,
        greeting: String,
        intro: String,
        cta: String,
        fallback: String,
        ignore: String,
        signature: String,
        magicLinkUrl: String,
    ) = """
        <!doctype html>
        <html lang="$lang">
          <body style="margin:0;padding:0;background:#f6f7f9;font-family:Arial,sans-serif;color:#1f2937;">
            <table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="padding:24px;">
              <tr>
                <td align="center">
                  <table role="presentation" width="600" cellspacing="0" cellpadding="0" style="background:#ffffff;border-radius:10px;padding:24px;">
                    <tr>
                      <td style="font-size:20px;font-weight:700;color:#37474f;">Thecla & Grégory</td>
                    </tr>
                    <tr><td style="height:16px;"></td></tr>
                    <tr>
                      <td style="font-size:16px;line-height:1.5;">
                        $greeting<br/><br/>
                        $intro
                      </td>
                    </tr>
                    <tr><td style="height:24px;"></td></tr>
                    <tr>
                      <td align="center">
                        <a href="${htmlEscape(magicLinkUrl)}" style="display:inline-block;background:#37474f;color:#ffffff;text-decoration:none;padding:12px 20px;border-radius:8px;font-weight:600;">
                          $cta
                        </a>
                      </td>
                    </tr>
                    <tr><td style="height:20px;"></td></tr>
                    <tr>
                      <td style="font-size:13px;color:#4b5563;line-height:1.5;">
                        $fallback<br/>
                        <a href="${htmlEscape(magicLinkUrl)}" style="color:#37474f;word-break:break-all;">${htmlEscape(magicLinkUrl)}</a>
                      </td>
                    </tr>
                    <tr><td style="height:12px;"></td></tr>
                    <tr>
                      <td style="font-size:13px;color:#6b7280;line-height:1.5;">
                        $ignore
                      </td>
                    </tr>
                    <tr><td style="height:20px;"></td></tr>
                    <tr>
                      <td style="font-size:13px;color:#6b7280;">$signature</td>
                    </tr>
                  </table>
                </td>
              </tr>
            </table>
          </body>
        </html>
    """.trimIndent()
}


