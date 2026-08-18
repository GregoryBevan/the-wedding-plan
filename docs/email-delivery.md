# Magic-link email delivery (SMTP vs Brevo)

The guest magic-link email is sent through a pluggable transport selected by `APP_MAIL_PROVIDER`:

- `smtp` (default) — classic SMTP via `JavaMailSender`. Good for local dev (Mailpit) and any host
  that permits outbound SMTP.
- `brevo` — Brevo's HTTPS transactional email API (`POST /v3/smtp/email`). **Use this on Render**,
  whose runtime blocks outbound SMTP ports (25/465/587), so SMTP connections time out. Because
  Brevo is called over HTTPS (443), it is unaffected.
- `noop` — no-op sender (sends nothing); useful for environments where email must be disabled.

In all cases delivery is best-effort: a provider failure is logged and swallowed so the
magic-link request always succeeds (the guest can retry).

## `APP_MAIL_PROVIDER=brevo`

Set:

- `BREVO_API_KEY` — Brevo transactional API key (mandatory; keep it secret)
- `APP_MAIL_FROM` — the From address; it must be a **verified sender/domain** in Brevo
- `BREVO_SENDER_NAME` (optional, default `Wedding Plan`)
- `BREVO_BASE_URL` (optional, default `https://api.brevo.com`)

## `APP_MAIL_PROVIDER=smtp`

Set the standard Spring mail vars: `SPRING_MAIL_HOST`, `SPRING_MAIL_PORT`,
`SPRING_MAIL_USERNAME`, `SPRING_MAIL_PASSWORD`, `SPRING_MAIL_SMTP_AUTH`,
`SPRING_MAIL_SMTP_STARTTLS_ENABLE`, and the `SPRING_MAIL_SMTP_*_TIMEOUT` values.

Locally, Mailpit acts as a permissive SMTP server — see
[Local development setup](local-development.md) for the end-to-end test scenario.

