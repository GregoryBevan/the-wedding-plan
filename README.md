[![The Wedding Plan CI](https://github.com/GregoryBevan/the-wedding-plan/actions/workflows/ci.yml/badge.svg)](https://github.com/GregoryBevan/the-wedding-plan/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/GregoryBevan/the-wedding-plan/branch/main/graph/badge.svg?token=GTPKWW4VSO)](https://codecov.io/gh/GregoryBevan/the-wedding-plan)

# The Wedding Plan

## Documentation

- [Guest magic-link authentication — operational & security constraints](docs/guest-magic-link-security.md)
- [Backoffice read-only permissions & role assignment](docs/backoffice-read-only-permissions.md)

## Deployment (Render + Aiven)

This repository now includes:

- `Dockerfile`: builds frontend + backend and ships a single runtime image.
- `render.yaml`: Render Blueprint with required runtime environment variables.

### Architecture

- Backend (Spring Boot) serves API under `/api/**`.
- Backoffice frontend is bundled into the backend jar and served under `/backoffice`.
- Database is external (Aiven PostgreSQL).

### 1) Create the Render web service from `render.yaml`

Render will build with Docker and run a single web service.

### 1.1) Enable deployment from GitHub after merge on `main`

This repository includes `.github/workflows/deploy.yml`.

- It triggers only when `The Wedding Plan CI` workflow succeeds on `main`.

Add this GitHub Actions secret in repository settings:

- `RENDER_DEPLOY_HOOK_URL`: Render deploy hook URL for your web service.

### 2) Configure runtime environment variables in Render

Set these variables in Render dashboard (or through Blueprint secrets), never in git:

- `SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_ID`
- `SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_SECRET`
- `APP_AUTH_ADMIN_EMAILS`
- `APP_AUTH_READ_ONLY_EMAILS` (optional; comma-separated emails for the **read-only** backoffice tier. Enforced server-side and reflected in the UI: these users can view every module's lists/detail (`GET`) but are blocked (`403`) from any create/edit/delete/archive/restore, and the backoffice hides write controls and redirects write routes for them. Admins in `APP_AUTH_ADMIN_EMAILS` keep full access. See [Backoffice read-only permissions](docs/backoffice-read-only-permissions.md))
- `APP_CORS_ALLOWED_ORIGINS`
- `APP_AUTH_SUCCESS_REDIRECT_URL`
- `POSTGRES_HOST`
- `POSTGRES_PORT`
- `POSTGRES_DB`
- `POSTGRES_USER`
- `POSTGRES_PASSWORD`
- `APP_MAIL_FROM`
- `APP_MAIL_PROVIDER` (magic-link email transport: `smtp` (default), `brevo`, or `noop`. Use **`brevo`** on Render — its runtime blocks outbound SMTP; see [Magic-link email delivery](#magic-link-email-delivery-smtp-vs-brevo))
- `APP_GUEST_ACCESS_BASE_URL`
- `APP_GUEST_AREA_URL`
- `SERVER_FORWARD_HEADERS_STRATEGY` (set to `framework` only when requests always come through a trusted proxy that strips/overwrites `Forwarded`/`X-Forwarded-*` headers; otherwise keep default `none`)
- `APP_DEEZER_ACCESS_TOKEN` (Deezer long-lived user access token with `manage_library` and `delete_library`; see [Deezer playlist sync](#deezer-playlist-sync-oauth))
- `APP_DEEZER_PLAYLIST_ID` (id of the shared Deezer playlist chosen songs are added to)

Email-transport variables depend on `APP_MAIL_PROVIDER` — see [Magic-link email delivery](#magic-link-email-delivery-smtp-vs-brevo) for the `brevo` (Render) and `smtp` sets.

Optional (Deezer song search — sensible defaults are provided, override only if needed):

- `APP_DEEZER_BASE_URL` (default `https://api.deezer.com`)
- `APP_DEEZER_CONNECT_TIMEOUT` (default `2s`)
- `APP_DEEZER_READ_TIMEOUT` (default `3s`)

### 3) OAuth callback

In Google OAuth app settings, configure callback URL:

`https://<your-render-domain>/login/oauth2/code/google`

### 4) Access the app

- Backoffice: `https://<your-render-domain>/backoffice`
- API base: same origin, under `/api`

### Magic-link email delivery (SMTP vs Brevo)

The guest magic-link email is sent through a pluggable transport selected by `APP_MAIL_PROVIDER`:

- `smtp` (default) — classic SMTP via `JavaMailSender`. Good for local dev (Mailpit) and any host
  that permits outbound SMTP.
- `brevo` — Brevo's HTTPS transactional email API (`POST /v3/smtp/email`). **Use this on Render**,
  whose runtime blocks outbound SMTP ports (25/465/587), so SMTP connections time out. Because Brevo
  is called over HTTPS (443), it is unaffected.
- `noop` — no-op sender (sends nothing); useful for environments where email must be disabled.

In all cases delivery is best-effort: a provider failure is logged and swallowed so the magic-link
request always succeeds (the guest can retry).

**When `APP_MAIL_PROVIDER=brevo`** set:

- `BREVO_API_KEY` — Brevo transactional API key (mandatory; keep it secret)
- `APP_MAIL_FROM` — the From address; it must be a **verified sender/domain** in Brevo
- `BREVO_SENDER_NAME` (optional, default `Wedding Plan`)
- `BREVO_BASE_URL` (optional, default `https://api.brevo.com`)

**When `APP_MAIL_PROVIDER=smtp`** set the standard Spring mail vars: `SPRING_MAIL_HOST`,
`SPRING_MAIL_PORT`, `SPRING_MAIL_USERNAME`, `SPRING_MAIL_PASSWORD`, `SPRING_MAIL_SMTP_AUTH`,
`SPRING_MAIL_SMTP_STARTTLS_ENABLE`, and the `SPRING_MAIL_SMTP_*_TIMEOUT` values.

### Deezer song search (autocomplete proxy)

Guests pick a song for the wedding playlist via an autocomplete backed by Deezer. Because Deezer's
API sends no CORS headers, the browser never calls Deezer directly: the backend proxies it through
the session-guarded endpoint `GET /api/guest-access/secured/song-search?q=...`, which queries Deezer
`GET https://api.deezer.com/search?q=...` and returns a slim suggestion list (`deezerId`, `title`,
`artist`, `link`, `preview`).

#### Deezer developer account

- Deezer's **search** endpoint is public and needs **no credentials**, so the proxy works out of the
  box with the defaults above.
- Playlist sync (below) does require a registered app and a user token. Create a Deezer developer
  account and application at <https://developers.deezer.com/myapps>. You'll obtain an **Application ID**
  and **Secret Key**; keep them in secret environment variables (never in git). They are only used to
  mint the access token during the one-time authorization described below — the running app never reads
  them directly.

### Deezer playlist sync (OAuth)

Every song a guest picks is mirrored to a **shared Deezer playlist** played on the wedding day. Adding a
track to a playlist requires the Deezer `manage_library` and `delete_library` permissions, so the couple authorizes the app
**once** and the resulting long-lived user access token is supplied to the backend as an environment
variable. There is no OAuth callback endpoint in the app: the token is obtained out-of-band (steps
below) and the backend simply reuses it.

The sync is best-effort and fully isolated from the RSVP: on submission the guest's answer is saved
first, then the track is added to the playlist (skipping it when already present, so there are no
duplicates). Conversely, when a guest **drops** their song (removes it, replaces it, or declines) the
track is removed from the playlist — unless another guest still chose it. Any Deezer failure is logged
and swallowed — the guest's RSVP always succeeds. Both the add and remove run asynchronously on a
background task thread, so the Deezer call never adds latency to the guest's response.

Each chosen song carries a `synchronized` flag in the stored answer; it flips to `true` only once the
track is confirmed on the playlist. A song whose sync failed (e.g. Deezer was down) stays pending, and
a **daily reconciliation task** re-drives every pending song so a picked track eventually always lands
on the playlist. Its schedule can be overridden with the optional `APP_PLAYLIST_RECONCILE_CRON`
environment variable (a Spring cron expression; default `0 0 3 * * *`, i.e. 03:00 daily).

Required environment variables:

- `APP_DEEZER_ACCESS_TOKEN` — long-lived Deezer user access token with `manage_library` (mandatory)
- `APP_DEEZER_PLAYLIST_ID` — id of the shared playlist to sync into (mandatory; it is the number in the
  playlist URL, e.g. `1234567890` in `https://www.deezer.com/playlist/1234567890`)

Both are **mandatory** (no defaults): the backend fails fast on startup if either is missing.

#### Obtaining a long-lived (infinite) access token — one time

Deezer OAuth is a two-legged, browser-based flow. Do this once as the couple's Deezer account owner:

1. In your Deezer app settings (<https://developers.deezer.com/myapps>), set the **Redirect URL after
   authentication** to any URL you control (it only needs to receive the `code` query param), e.g.
   `http://localhost:8080/`.
2. In a browser, authorize the app and request an **infinite** token by passing `expiration=0`:

   ```
   https://connect.deezer.com/oauth/auth.php?app_id=<APP_ID>&redirect_uri=<REDIRECT_URI>&perms=basic_access,manage_library&expiration=0
   ```

   Approve the permissions. Deezer redirects to `<REDIRECT_URI>?code=<CODE>`; copy the `code`.
3. Exchange the `code` for the access token (e.g. with `curl`):

   ```bash
   curl "https://connect.deezer.com/oauth/access_token.php?app_id=<APP_ID>&secret=<APP_SECRET>&code=<CODE>&output=json"
   ```

   The response contains `access_token` (and `expires: 0` for an infinite token). Use that
   `access_token` value as `APP_DEEZER_ACCESS_TOKEN`.
4. Create (or pick) the shared playlist on the couple's account and use its id as `APP_DEEZER_PLAYLIST_ID`.

If the token is ever revoked, repeat the steps and update `APP_DEEZER_ACCESS_TOKEN`.

#### Adding other providers later

The proxy is intentionally scoped to a single provider today. Support for Spotify, YouTube Music or
Apple Music would be added as separate, similarly configured integrations when needed — no
provider-agnostic abstraction is introduced up front (YAGNI).

### 5) Local email delivery test scenario (Mailpit + Bruno)

This project provides a guest-access magic-link flow that is protected by CSRF. To test email delivery locally, use Mailpit + the Bruno collection under `backend/http/Wedding Plan`.

#### Prerequisites

- Start local dependencies:
  - `docker compose up -d db mailpit`
- Start backend locally (IntelliJ run config or `./gradlew bootRun` from `backend`).
- In Bruno, set environment variables:
  - `backend_url` (for example `http://localhost:8080`)
  - `invitation_token` (a valid token from your local DB)

Optional SQL to get a token:

`SELECT access_token FROM invitations ORDER BY creation_date DESC LIMIT 1;`

#### Required request order

Run requests in this exact order:

1. `Get guest invitation detail`
2. `Bootstrap csrf`
3. `Request magik link`

#### Why this order is mandatory

- `Get guest invitation detail`:
  - Fetches invitation data and captures a valid `guestId` for the selected invitation.
  - Without this step, the final request has no guaranteed valid guest identifier.

- `Bootstrap csrf`:
  - Intentionally performs a POST before CSRF headers are set.
  - Spring Security responds with `403`, but also issues the CSRF/session cookies (`XSRF-TOKEN`, `JSESSIONID`).
  - The Bruno script stores those values into `cookieHeader` and `xsrfToken`.

- `Request magik link`:
  - Replays the same POST with `Cookie` + `X-XSRF-TOKEN` headers.
  - This is the first request expected to return `202` and trigger the email send path.

If step 2 is skipped, step 3 is expected to fail with `403` because CSRF/session state is missing.

#### Validate email reception

- Open Mailpit UI: `http://localhost:8025`
- Verify that a message is received for the selected guest.
- Verify body contains a guest-scoped link:
  - `/api/guest-access/magic-links/{token}`

### Security notes

- Never commit `.env` production secrets.
- Keep all credentials in Render secret environment variables.
- Avoid printing sensitive env vars in logs, CI summaries, or scripts.
- For IP-based protections (rate limiting), do not trust forwarded headers unless your ingress/proxy sanitizes them.
