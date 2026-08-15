# Backoffice read-only permissions & rollout guide

This document is the maintainer reference for the **global backoffice read-only model** (epic #176). It
explains the permission model and role assignment, gives a step-by-step guide for onboarding a **new
backoffice module** to the standard, and provides a troubleshooting + verification checklist. The goal:
a contributor can apply the pattern to any module **without tribal knowledge**.

> **Terminology.** "Backoffice" = the Google-authenticated staff area served under `/backoffice` and
> its API under `/api/**` (Invitations, Guests, and any future module). It is *not* the wedding
> attendees' self-service magic-link flow — that separate path is covered in
> [Guest self-service access](#guest-self-service-access-separate-path).

## Status

The model is **live**: read-only assignment, server-side enforcement, and read-only UI rendering all
ship. `GET /auth/me` returns JSON fields `authenticated`, `email`, `authorized` (true for admin **and**
read-only — i.e. may access the backoffice) and `canWrite` (true for admins only); the frontend keys
write affordances and write-route guards off `canWrite`.

Remaining work — migrating any not-yet-covered modules onto the global rules — is tracked in the epic
(#176).

## Roles & backoffice capabilities

Backoffice access is granted through Google OAuth2 login combined with email allowlists. Access is
expressed as two **positive** capabilities shared by every module — `backoffice.read` and
`backoffice.write` — and each role grants a subset:

- **Admin** — grants **both** `backoffice.read` and `backoffice.write` (full access). Assigned via
  `app.auth.admin-emails` (`APP_AUTH_ADMIN_EMAILS`).
- **Read-only** — grants only `backoffice.read`: may view every backoffice list/detail but
  cannot perform any mutation. Assigned via `app.auth.read-only-emails` (`APP_AUTH_READ_ONLY_EMAILS`).

A signed-in Google user whose email is in **neither** list is *unauthorized* and receives `403` on any
`/api/**` call.

**Precedence (most-privileged wins):** if the same email appears in both lists, **admin wins** — the
read-only capability never downgrades a more privileged role. If additional privileged roles (e.g. an
`editor`) are introduced later, they slot in above read-only under the same rule.

## Global policy — how enforcement works


Enforcement is **verb-based and global**, not wired per endpoint. Applied to every backoffice `/api/**`
route:

| Caller | Safe methods (`GET`, `HEAD`) | Mutating methods (`POST`, `PUT`, `PATCH`, `DELETE`) |
| --- | :---: | :---: |
| Admin | ✅ | ✅ |
| Read-only (`backoffice.read`) | ✅ | ❌ `403` |
| Unauthorized | ❌ `403` | ❌ `403` |

Because the rule keys off the HTTP verb, **any new module that follows REST conventions is covered
automatically** — reads use `GET`, writes use `POST`/`PUT`/`PATCH`/`DELETE`. Blocking happens at the
**API layer** (server-side), so hiding UI controls is defense-in-depth, not the only guard.

The check runs in the security filter chain **before request routing**, and any verb that is not `GET`/`HEAD`
is treated as a write (fail-closed). A read-only user therefore gets `403` even on a verb or path a module
never declared (e.g. `PATCH /api/guests/{id}`, or `DELETE /api/invitations/{id}` where no delete route
exists) — there is no bypass via direct URL or verb manipulation.

### Illustrative matrix

Concrete examples of the global rule on the two current modules:

**Invitations (`/api/invitations`)**

| Action | HTTP | Admin | Read-only | Unauthorized |
| --- | --- | :---: | :---: | :---: |
| List / detail | `GET /api/invitations`, `GET /api/invitations/{id}` | ✅ | ✅ | ❌ `403` |
| Create / update | `POST /api/invitations`, `PUT /api/invitations/{id}` | ✅ | ❌ `403` | ❌ `403` |

**Guests (`/api/guests`)**

| Action | HTTP | Admin | Read-only | Unauthorized |
| --- | --- | :---: | :---: | :---: |
| List / detail | `GET /api/guests`, `GET /api/guests/{id}` | ✅ | ✅ | ❌ `403` |
| Create / update | `POST /api/guests`, `PUT /api/guests/{id}` | ✅ | ❌ `403` | ❌ `403` |
| Archive / restore | `DELETE /api/guests/{id}`, `POST /api/guests/{id}/restoration` | ✅ | ❌ `403` | ❌ `403` |

Note `POST /api/guests/{id}/restoration` is a mutation and is blocked by the verb rule like any other
`POST` — no special handling needed.

## Guest self-service access (separate path)

The roles above govern the **backoffice** (Google-authenticated staff). Wedding attendees read *their
own* invitation through a completely separate, public endpoint — this is **not** a backoffice module:

| Action | HTTP | Authorized by |
| --- | --- | --- |
| Resolve own invitation | `GET /api/guest-access/invitations/{token}` | Possession of the invitation **access token** (the QR/link token) |

This path is **not** part of the Admin / read-only model:

- It is `permitAll` at the security layer (`/api/guest-access/**`); authorization is *capability-based* —
  knowing the unguessable invitation access token is the credential. It returns only that one
  invitation's view, never the list.
- Attendee **mutations** (RSVP, song choices under `/api/guest-access/secured/**`) require a magic-link
  guest session, described in [Guest magic-link authentication](guest-magic-link-security.md).
- `APP_AUTH_READ_ONLY_EMAILS` and `APP_AUTH_ADMIN_EMAILS` have **no effect** on this path.

In short: backoffice access is gated by staff **roles**; attendee access is gated by **token
possession**. The two are independent.

## Assigning the read-only role

Assignment is configuration-only; no database change or code redeploy is required.

1. Set the read-only allowlist as a comma-separated list of emails (case-insensitive, whitespace is
   trimmed). In Render, add it as a runtime environment variable (never commit real emails to git):

   ```
   APP_AUTH_READ_ONLY_EMAILS=viewer1@example.com,viewer2@example.com
   ```

   Locally, set the same value via `app.auth.read-only-emails` in your environment or an untracked
   override.

2. Ensure the email is **not** also in `APP_AUTH_ADMIN_EMAILS` unless you intend a full admin (admin
   precedence applies).

3. Restart / redeploy so the configuration is picked up.

4. The user signs in at `/backoffice` with Google and is authorized: they can read every module but any
   write returns `403` (enforced server-side). `GET /auth/me` reports `authorized: true` and
   `canWrite: false`, so the UI renders every module in read-only mode — write affordances are hidden and
   write routes redirect to access-denied.

To **revoke**, remove the email from `APP_AUTH_READ_ONLY_EMAILS` and restart.

## Onboarding a new backoffice module (rollout guide)

Because the policy is global and verb-based, onboarding a module is mostly *verifying* the conventions
hold. For each new module:

1. **Follow REST verbs.** Reads use `GET`/`HEAD`; every mutation uses `POST`/`PUT`/`PATCH`/`DELETE`.
   If you do this, the global server-side policy blocks writes for read-only users automatically — no
   per-route wiring needed.
2. **Route under `/api/**`.** The global filter only covers backoffice API routes; keep the module there
   so it inherits the policy.
3. **Use the shared UI guard.** Render write affordances (create/edit/delete buttons, menu items,
   toolbars) through the reusable permission-aware guard (`WriteOnly` / `useCapabilities`) so they are
   hidden for read-only users. Do **not** re-implement per-module permission checks.
4. **Key the UI off `/auth/me`.** Use the capability reported there rather than hard-coding email
   checks in components.
5. **Handle exceptions explicitly** (see next section) if the module has a non-RESTful verb.
6. **Add tests**: (a) server-side — a read-only caller gets `200` on `GET` and `403` on each mutating
   verb; (b) UI — write affordances are hidden in read-only mode and visible for admins.
7. **Tick the module off** the rollout checklist in epic #176.

## Exceptions policy

The verb rule assumes REST conventions. A route that reads via a mutating verb (e.g. a **search that
uses `POST`**) would be wrongly blocked, and a mutation exposed via `GET` would wrongly be allowed. Such
cases MUST be:

- avoided when feasible (prefer `GET` for reads), or
- explicitly allowlisted/denied in the global policy **and documented here** with the rationale, so the
  exception is tracked rather than implicit (epic #176: "Track exceptions explicitly").

_Current exceptions: none._

## Troubleshooting

- **A read-only user sees a write button.** The component isn't using the shared UI guard, or it hard-codes
  visibility. Route it through the permission-aware guard and key off `/auth/me`.
- **A read-only user still succeeds on a write via the API.** The route uses a non-mutating verb for a
  mutation (e.g. a write behind `GET`), or sits outside `/api/**`. Fix the verb/placement or add a
  documented exception.
- **An admin gets `403`.** Their email isn't in `APP_AUTH_ADMIN_EMAILS` (check casing/whitespace — the
  list is normalized) or the app didn't pick up the new env value (restart/redeploy).
- **A read-only user gets `403` on a legitimate read.** Confirm the read uses `GET`/`HEAD` and the email
  is in `APP_AUTH_READ_ONLY_EMAILS`; if it's a `POST`-based read, it needs a documented exception.
- **`/auth/me` shows not authorized after adding the email.** The change requires a restart/redeploy;
  also verify the user signed in with the exact Google email that was allowlisted.

## Verification checklist

Sign in with a **read-only** account and confirm:

- [ ] Every backoffice module's **list** and **detail** pages load.
- [ ] No **create / edit / delete / archive / restore** control is visible in any list, detail, or
      navigation menu.
- [ ] Each mutating endpoint returns `403` when called directly (bypassing the UI), for every module —
      e.g. `POST /api/invitations`, `PUT /api/invitations/{id}`, `POST /api/guests`,
      `PUT /api/guests/{id}`, `DELETE /api/guests/{id}`, `POST /api/guests/{id}/restoration`.

Sign in with an **admin** account and confirm nothing regressed:

- [ ] Lists and details load across modules.
- [ ] Write controls are visible and functional; write calls succeed.

Sign in with an **unauthorized** Google account (in neither list) and confirm:

- [ ] `GET` on any backoffice module (e.g. `/api/invitations`, `/api/guests`) returns `403`.

## Release notes

> **Backoffice read-only role.** Operators can now grant view-only backoffice access by adding emails to
> `APP_AUTH_READ_ONLY_EMAILS`. These users can browse all backoffice modules (lists and details) but
> cannot create, edit, delete, archive, or restore anything. Admins listed in `APP_AUTH_ADMIN_EMAILS`
> keep full access; an email present in both lists remains a full admin.

