## Plan: Add Vue Router To Backoffice

Introduce `vue-router` to make navigation URL-driven (`/guests`, `/guests/new`) instead of local `activeView` state, so browser back/forward, deep-linking, and future pages work consistently. Keep auth gating in `App` shell, move page content to route components, and adapt existing tests from button-driven view toggles to route assertions. This is a higher-effort but scalable foundation if more backoffice sections are expected.

### Steps
1. Add `vue-router` dependency in `frontend/package.json` and create backoffice router module in `frontend/backoffice/src/router/index.ts`.
2. Define route records for guest pages (`/guests`, `/guests/new`) using `GuestList` and a dedicated add page wrapping `GuestForm`.
3. Update `frontend/backoffice/src/main.ts` to register router, then refactor `frontend/backoffice/src/App.vue` to use `RouterLink` and `RouterView`.
4. Replace `activeView`/`switchView` logic in `frontend/backoffice/src/App.vue` with route-aware message reset and submit flow (`useAddRequest`).
5. Update `frontend/backoffice/src/App.spec.ts` to mount with router, assert route transitions, and preserve auth/logout behavior across guarded views.

### Further Considerations
1. Route shape decision: Option A flat (`/guests`, `/guests/new`) vs Option B nested (`/backoffice/guests`, `/backoffice/guests/new`) vs Option C name-only with aliases.
2. Auth handling choice: Option A guard in `App.vue` (minimal change) vs Option B router `beforeEach` guard (cleaner long-term).
3. Draft for review: keep `GuestList` and add form as separate pages after router migration, or still switch to modal once routing is in place?

