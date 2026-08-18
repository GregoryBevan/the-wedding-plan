# Local development setup

This guide covers running the backend and frontend locally, plus a scenario to validate guest
magic-link email delivery end-to-end.

## Prerequisites

- JDK 25 (managed via [SDKMAN](https://sdkman.io/)) for the backend.
- Node.js and [pnpm](https://pnpm.io/) for the frontend.
- Docker (for PostgreSQL and Mailpit via `docker-compose.yml`).

## Start local dependencies

```bash
docker compose up -d db mailpit
```

- `db`: PostgreSQL, seeded from `docker/postgres/init`.
- `mailpit`: local SMTP server + web UI (`http://localhost:8025`) to inspect emails sent by the
  backend during development (magic-link emails).

## Backend

All backend code lives in `backend/`.

- Run the app: IntelliJ run configuration, or `./gradlew bootRun` from `backend`.
- Run unit tests: `./gradlew test`
- Run integration tests (Testcontainers PostgreSQL): `./gradlew integrationTest`
- Run full verification: `./gradlew check`

By default the backend uses the `smtp` mail provider (`APP_MAIL_PROVIDER=smtp`), which sends
through Mailpit locally. See [Email delivery](email-delivery.md) for provider configuration.

## Frontend

All frontend code lives in `frontend/`. There are two Vite apps: the public guest-facing app
(`frontend/public`) and the backoffice (`frontend/backoffice`).

- Install dependencies: `pnpm install`
- Run the public app: `pnpm run dev:public`
- Run the backoffice app: `pnpm run dev:backoffice`
- Run tests: `pnpm run test`
- Run tests in watch mode: `pnpm run test:watch`
- Run tests with coverage: `pnpm run test:coverage`

### Environment variables

Env files are gitignored (`.env.*`). Create them locally before running the backoffice app.

**`frontend/backoffice/.env.development`**

```
VITE_API_BASE_URL=http://localhost:8080
VITE_ROUTER_BASE=/
```

**`frontend/backoffice/.env.production`**

```
VITE_API_BASE_URL=https://your-api-domain.com
VITE_ROUTER_BASE=/backoffice/
```

> `VITE_ROUTER_BASE` must match the path the backoffice is served under. In production the
> backoffice lives at `/backoffice/`; in development it is served at the root `/`.

## Guest magic-link email delivery test scenario (Mailpit + Bruno)

The guest-access magic-link flow is protected by CSRF. To test email delivery locally, use Mailpit
plus the Bruno collection under `backend/http/Wedding Plan`.

### Prerequisites

- Start local dependencies: `docker compose up -d db mailpit`
- Start the backend locally (IntelliJ run config or `./gradlew bootRun` from `backend`).
- In Bruno, set environment variables:
  - `backend_url` (for example `http://localhost:8080`)
  - `invitation_token` (a valid token from your local DB)

Optional SQL to get a token:

```sql
SELECT access_token FROM invitation ORDER BY creation_date DESC LIMIT 1;
```

### Required request order

Run requests in this exact order:

1. `Get guest invitation detail`
2. `Bootstrap csrf`
3. `Request magic link` (Bruno request name: `Request magik link`)

### Why this order is mandatory

- **`Get guest invitation detail`**: fetches invitation data and captures a valid `guestId` for the
  selected invitation. Without this step, the final request has no guaranteed valid guest
  identifier.
- **`Bootstrap csrf`**: intentionally performs a POST before CSRF headers are set. Spring Security
  responds with `403`, but also issues the CSRF/session cookies (`XSRF-TOKEN`, `JSESSIONID`). The
  Bruno script stores those values into `cookieHeader` and `xsrfToken`.
- **`Request magik link`**: replays the same POST with `Cookie` + `X-XSRF-TOKEN` headers. This is
  the first request expected to return `202` and trigger the email send path.

If step 2 is skipped, step 3 is expected to fail with `403` because CSRF/session state is missing.

### Validate email reception

- Open Mailpit UI: `http://localhost:8025`
- Verify that a message is received for the selected guest.
- Verify body contains a guest-scoped link: `/api/guest-access/magic-links/{token}`

