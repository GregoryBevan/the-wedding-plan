# Deezer integration

Guests pick a song for the wedding playlist as part of their RSVP. This covers the search proxy
used for autocomplete and the playlist sync that mirrors chosen songs.

## Song search (autocomplete proxy)

Deezer's API sends no CORS headers, so the browser never calls it directly: the backend proxies
search through the session-guarded `GET /api/guest-access/secured/song-search?q=...`, which calls
Deezer's `GET https://api.deezer.com/search?q=...` and returns a slim suggestion list (`deezerId`,
`title`, `artist`, `link`, `preview`).

Deezer's search endpoint is public and needs **no credentials**, so this works out of the box.
Optional overrides (sensible defaults provided): `APP_DEEZER_BASE_URL` (default
`https://api.deezer.com`), `APP_DEEZER_CONNECT_TIMEOUT` (default `2s`),
`APP_DEEZER_READ_TIMEOUT` (default `3s`).

## Playlist sync (OAuth)

Every song a guest picks is mirrored to a **shared Deezer playlist** played on the wedding day:
adding a track needs `manage_library`, removing one (guest drops/replaces their song) needs
`delete_library`. The couple authorizes the app **once** and the resulting long-lived access
token is supplied to the backend as an environment variable — there is no OAuth callback endpoint;
the token is obtained out-of-band (steps below) and reused.

The sync is best-effort and isolated from the RSVP: the answer is saved first, then the track is
added (skipped if already present, no duplicates) or removed — unless another guest still chose
it. Any Deezer failure is logged and swallowed, so the RSVP always succeeds. Both operations run
asynchronously on a background thread, adding no latency.

Each song carries a `synchronized` flag, set once the track is confirmed on the playlist. A song
whose sync failed stays pending, and a **daily reconciliation task** re-drives every pending song
(overridable via `APP_PLAYLIST_RECONCILE_CRON`, default `0 0 3 * * *`).

### Required environment variables

- `APP_DEEZER_ACCESS_TOKEN` — long-lived Deezer user access token with `manage_library` and
  `delete_library` (mandatory, no default)
- `APP_DEEZER_PLAYLIST_ID` — id of the shared playlist to sync into (mandatory, no default; the
  number in the playlist URL, e.g. `1234567890` in `https://www.deezer.com/playlist/1234567890`)

The backend fails fast on startup if either is missing.

### Obtaining a long-lived access token — one time

Do this once as the couple's Deezer account owner, using a registered app at
<https://developers.deezer.com/myapps> (gives an **Application ID** + **Secret Key** — keep them
secret; they're only used below, the running app never reads them directly):

1. Set the app's **Redirect URL after authentication** to any URL you control (it just needs to
   receive the `code` query param), e.g. `http://localhost:8080/`.
2. In a browser, authorize with the full scope — `offline_access` alone already yields an
   **infinite** token, no `expiration` param needed:

   ```
   https://connect.deezer.com/oauth/auth.php?app_id=<APP_ID>&redirect_uri=<REDIRECT_URI>&perms=basic_access,email,offline_access,manage_library,delete_library
   ```

   Deezer redirects to `<REDIRECT_URI>?code=<CODE>`; copy the `code`.
3. Exchange it for the access token:

   ```bash
   curl "https://connect.deezer.com/oauth/access_token.php?app_id=<APP_ID>&secret=<APP_SECRET>&code=<CODE>&output=json"
   ```

   Use the returned `access_token` (with `expires: 0`) as `APP_DEEZER_ACCESS_TOKEN`.
4. Create (or pick) the shared playlist and use its id as `APP_DEEZER_PLAYLIST_ID`.

If the token is ever revoked, repeat the steps and update `APP_DEEZER_ACCESS_TOKEN`.

## Adding other providers later

The proxy is intentionally scoped to a single provider today. Spotify, YouTube Music, or Apple
Music would be added as separate, similarly configured integrations when needed — no
provider-agnostic abstraction up front (YAGNI).
