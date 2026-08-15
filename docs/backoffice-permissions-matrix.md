# Backoffice permissions matrix (resource × action)

This is the **authoritative resource × action reference** for the global backoffice read-only model
(epic #176). It enumerates every backoffice module, classifies each action, and states the intended
behavior for each caller role. Backend enforcement and UI sub-issues reference this matrix as the single
source of truth; the model itself (roles, enforcement mechanism, onboarding) is described in
[Backoffice read-only permissions & rollout guide](backoffice-read-only-permissions.md).

## Roles

| Role | Capabilities | Assigned via |
| --- | --- | --- |
| **Admin** | `backoffice.read` + `backoffice.write` | `APP_AUTH_ADMIN_EMAILS` |
| **Read-only** | `backoffice.read` | `APP_AUTH_READ_ONLY_EMAILS` |
| **Unauthorized** | none (authenticated Google user in neither list) | — |

Legend: ✅ allowed · ❌ `403 Forbidden`. "Read-only intent" states whether the action is meant to be
available to a read-only user.

## Action taxonomy

Each backoffice action is classified as one of: **list**, **show**, **create**, **update**, **delete**,
or **custom** (a non-CRUD mutation). Reads (`list`, `show`) use safe HTTP verbs (`GET`/`HEAD`); every
other type is a write and uses `POST`/`PUT`/`PATCH`/`DELETE`.

## Matrix — backoffice modules (`/api/**`)

### Guests (`/api/guests`)

| Action | Type | HTTP | Admin | Read-only | Unauthorized | Read-only intent |
| --- | --- | --- | :---: | :---: | :---: | --- |
| List guests | list | `GET /api/guests` | ✅ | ✅ | ❌ `403` | Allowed (read) |
| Show guest | show | `GET /api/guests/{id}` | ✅ | ✅ | ❌ `403` | Allowed (read) |
| Create guest | create | `POST /api/guests` | ✅ | ❌ `403` | ❌ `403` | Denied (write) |
| Update guest | update | `PUT /api/guests/{id}` | ✅ | ❌ `403` | ❌ `403` | Denied (write) |
| Archive guest | delete | `DELETE /api/guests/{id}` | ✅ | ❌ `403` | ❌ `403` | Denied (write) |
| Restore guest | custom | `POST /api/guests/{id}/restoration` | ✅ | ❌ `403` | ❌ `403` | Denied (write) |

### Invitations (`/api/invitations`)

| Action | Type | HTTP | Admin | Read-only | Unauthorized | Read-only intent |
| --- | --- | --- | :---: | :---: | :---: | --- |
| List invitations | list | `GET /api/invitations` | ✅ | ✅ | ❌ `403` | Allowed (read) |
| Show invitation | show | `GET /api/invitations/{id}` | ✅ | ✅ | ❌ `403` | Allowed (read) |
| Create invitation | create | `POST /api/invitations` | ✅ | ❌ `403` | ❌ `403` | Denied (write) |
| Update invitation | update | `PUT /api/invitations/{id}` | ✅ | ❌ `403` | ❌ `403` | Denied (write) |

## Ambiguous actions — explicit policy decisions

These are the non-obvious cases; each has a deliberate, recorded decision so behavior is not left
implicit.

1. **Guest archive uses `DELETE` but is a soft-delete (archive), not a hard delete.** Policy: it is a
   **mutation** and is therefore **denied** for read-only users. The soft vs hard distinction does not
   change the permission — any state change is a write.
2. **Guest restore is a custom action (`POST …/restoration`), not standard CRUD.** Policy: it is a
   **mutation** and is **denied** for read-only users. Custom mutations must use a writing verb so the
   global verb rule covers them without special handling.
3. **Invitations has no delete route.** Policy: none is planned. Should a client still issue
   `DELETE /api/invitations/{id}`, the global rule **fail-closes** (any non-`GET`/`HEAD` verb ⇒ write)
   and returns `403` for read-only users — there is no bypass via an undeclared verb/path.
4. **Invitation QR code preview & PNG/SVG download.** These are generated **entirely client-side** from
   the invitation's access token (no API mutation). Policy: this is a **read/derived affordance** and is
   therefore **allowed** for read-only users. It does not create or modify server state.
5. **List query filters** (e.g. guest `availability`/`status`, pagination). Policy: query parameters on a
   `GET` are **reads** and are **allowed** for read-only users.

_No action currently reads through a mutating verb or writes through a safe verb. If such a case is
introduced, it must be added here with a rationale (see the Exceptions policy in the rollout guide)._

## Out of scope — not governed by this matrix

The following paths are **not** backoffice modules and are intentionally excluded from the admin /
read-only model:

| Path | Purpose | Authorized by |
| --- | --- | --- |
| `GET /auth/me` | Report session status + capabilities | Any session (returns `authenticated`/`authorized`/`canWrite`) |
| `POST /auth/logout` | End the staff session | Any authenticated session |
| `GET /api/guest-access/invitations/{token}` | Attendee resolves their own invitation | Possession of the invitation access token |
| `GET /api/guest-access/magic-links/{token}` · `POST /api/guest-access/invitations/{token}/guests/{guestId}/magic-link-requests` | Attendee magic-link issuance/verification | Token possession |
| `/api/guest-access/secured/**` (RSVP, song search) | Attendee self-service mutations/reads | Magic-link guest session |

`APP_AUTH_ADMIN_EMAILS` / `APP_AUTH_READ_ONLY_EMAILS` have **no effect** on these paths. See
[Guest magic-link authentication](guest-magic-link-security.md).

## How this matrix is enforced

- **Backend:** a single verb-based rule on `/api/**` (safe verbs ⇒ `backoffice.read`, all others ⇒
  `backoffice.write`), evaluated in the security filter chain before routing. Every "Denied (write)" row
  is covered without per-endpoint wiring.
- **UI:** write affordances are rendered through the shared `WriteOnly` / `useCapabilities` guard and
  write routes require `canWrite`; every "Allowed (read)" row stays visible to read-only users.

Any new backoffice module must extend this matrix (one row per action) as part of its rollout, keeping it
complete for all current modules.

