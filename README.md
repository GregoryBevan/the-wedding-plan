[![The Wedding Plan CI](https://github.com/GregoryBevan/the-wedding-plan/actions/workflows/ci.yml/badge.svg)](https://github.com/GregoryBevan/the-wedding-plan/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/GregoryBevan/the-wedding-plan/branch/main/graph/badge.svg?token=GTPKWW4VSO)](https://codecov.io/gh/GregoryBevan/the-wedding-plan)

# The Wedding Plan

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
- `APP_AUTH_ALLOWED_EMAILS`
- `APP_CORS_ALLOWED_ORIGINS`
- `APP_AUTH_SUCCESS_REDIRECT_URL`
- `POSTGRES_HOST`
- `POSTGRES_PORT`
- `POSTGRES_DB`
- `POSTGRES_USER`
- `POSTGRES_PASSWORD`
- `APP_MAIL_FROM`
- `APP_MAIL_ENABLED`
- `APP_GUEST_ACCESS_BASE_URL`
- `APP_GUEST_AREA_URL`
- `SPRING_MAIL_HOST`
- `SPRING_MAIL_PORT`
- `SPRING_MAIL_USERNAME`
- `SPRING_MAIL_PASSWORD`
- `SPRING_MAIL_SMTP_AUTH`
- `SPRING_MAIL_SMTP_STARTTLS_ENABLE`
- `SPRING_MAIL_SMTP_CONNECTION_TIMEOUT`
- `SPRING_MAIL_SMTP_TIMEOUT`
- `SPRING_MAIL_SMTP_WRITE_TIMEOUT`
- `SERVER_FORWARD_HEADERS_STRATEGY` (set to `framework` only when requests always come through a trusted proxy that strips/overwrites `Forwarded`/`X-Forwarded-*` headers; otherwise keep default `none`)

### 3) OAuth callback

In Google OAuth app settings, configure callback URL:

`https://<your-render-domain>/login/oauth2/code/google`

### 4) Access the app

- Backoffice: `https://<your-render-domain>/backoffice`
- API base: same origin, under `/api`

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
  - `/guest-access/magic-links/{token}`

### Security notes

- Never commit `.env` production secrets.
- Keep all credentials in Render secret environment variables.
- Avoid printing sensitive env vars in logs, CI summaries, or scripts.
- For IP-based protections (rate limiting), do not trust forwarded headers unless your ingress/proxy sanitizes them.
