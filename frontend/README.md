### Frontend development instructions

- Use `pnpm` from this absolute path: `/opt/homebrew/bin/pnpm`.
- Install dependencies:
  - `/opt/homebrew/bin/pnpm install`
- Run backoffice in development:
  - `/opt/homebrew/bin/pnpm run dev:backoffice`
- Run tests:
  - `/opt/homebrew/bin/pnpm run test`

### Environment variables

Env files are gitignored (`.env.*`). Create them locally before running the app.

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

> `VITE_ROUTER_BASE` must match the path the backoffice is served under.
> In production the backoffice lives at `/backoffice/`; in development it is served at the root `/`.
