package me.elgregos.theweddingplan.infrastructure.guest.service

import org.springframework.stereotype.Component

@Component
class GuestMagicLinkEmailTemplate {

    fun subject() = "Thecla & Grégory - Votre invitation"

    fun textBody(guestFirstName: String, magicLinkUrl: String) = """
        Bonjour $guestFirstName,

        Nous sommes heureux de vous inviter et ravis de vous partager votre lien sécurisé pour accéder à votre invitation :
        $magicLinkUrl

        Si vous n'êtes pas à l'origine de cette demande, vous pouvez ignorer cet email.

        --
        L'équipe The Wedding Plan
    """.trimIndent()

    fun htmlBody(guestFirstName: String, magicLinkUrl: String) = """
        <!doctype html>
        <html lang="fr">
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
                        Bonjour $guestFirstName,<br/><br/>
                        Nous sommes heureux de vous inviter et ravis de vous partager votre lien sécurisé pour accéder à votre invitation.
                      </td>
                    </tr>
                    <tr><td style="height:24px;"></td></tr>
                    <tr>
                      <td align="center">
                        <a href="$magicLinkUrl" style="display:inline-block;background:#37474f;color:#ffffff;text-decoration:none;padding:12px 20px;border-radius:8px;font-weight:600;">
                          Accéder à mon invitation
                        </a>
                      </td>
                    </tr>
                    <tr><td style="height:20px;"></td></tr>
                    <tr>
                      <td style="font-size:13px;color:#4b5563;line-height:1.5;">
                        Si le bouton ne fonctionne pas, copiez-collez ce lien dans votre navigateur :<br/>
                        <a href="$magicLinkUrl" style="color:#37474f;word-break:break-all;">$magicLinkUrl</a>
                      </td>
                    </tr>
                    <tr><td style="height:12px;"></td></tr>
                    <tr>
                      <td style="font-size:13px;color:#6b7280;line-height:1.5;">
                        Si vous n'êtes pas à l'origine de cette demande, vous pouvez ignorer cet email.
                      </td>
                    </tr>
                    <tr><td style="height:20px;"></td></tr>
                    <tr>
                      <td style="font-size:13px;color:#6b7280;">-- L'équipe The Wedding Plan</td>
                    </tr>
                  </table>
                </td>
              </tr>
            </table>
          </body>
        </html>
    """.trimIndent()
}


