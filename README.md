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
- `SERVER_FORWARD_HEADERS_STRATEGY` (set to `framework` only when requests always come through a trusted proxy that strips/overwrites `Forwarded`/`X-Forwarded-*` headers; otherwise keep default `none`)

### 3) OAuth callback

In Google OAuth app settings, configure callback URL:

`https://<your-render-domain>/login/oauth2/code/google`

### 4) Access the app

- Backoffice: `https://<your-render-domain>/backoffice`
- API base: same origin, under `/api`

### Security notes

- Never commit `.env` production secrets.
- Keep all credentials in Render secret environment variables.
- Avoid printing sensitive env vars in logs, CI summaries, or scripts.
- For IP-based protections (rate limiting), do not trust forwarded headers unless your ingress/proxy sanitizes them.
