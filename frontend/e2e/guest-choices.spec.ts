import { expect, test, type Page, type Route } from '@playwright/test';
import { fulfillJson } from './fixtures/httpHelpers';
import { UI_TIMEOUT_MS } from './fixtures/timeouts';

const PUBLIC_BASE_URL = 'http://127.0.0.1:4174';

// The `/me`, `/rsvp` and `/song-search` requests are sent with `credentials: 'include'`,
// so the browser requires the exact origin echoed back with `allow-credentials`.
const CREDENTIALED_CORS = {
  'access-control-allow-origin': PUBLIC_BASE_URL,
  'access-control-allow-credentials': 'true',
};

// The RSVP submission is a credentialed POST with a JSON body, which triggers a
// CORS preflight the mocked endpoint must acknowledge.
const PREFLIGHT_CORS = {
  ...CREDENTIALED_CORS,
  'access-control-allow-methods': 'GET,POST,OPTIONS',
  'access-control-allow-headers': 'content-type,x-xsrf-token',
};

type Attendance = 'ATTENDING' | 'DECLINED';
type Meal = 'MEAT' | 'FISH' | 'VEGGIE';

interface Song {
  deezerId: number;
  title: string;
  artist: string;
  link: string;
  preview?: string | null;
}

// `preview: null` keeps the preview button (and its <audio>) out of the way so the
// test never depends on flaky media playback.
const laVieEnRose: Song = {
  deezerId: 3135556,
  title: 'La Vie en rose',
  artist: 'Edith Piaf',
  link: 'https://www.deezer.com/track/3135556',
  preview: null,
};

const savedRsvp = (attendance: Attendance, meal: Meal | null = null, song: Song | null = null) => ({
  id: 'rsvp-1',
  version: 1,
  creationDate: '2026-06-13T10:00:00',
  updateDate: '2026-06-13T10:00:00',
  attendance,
  meal,
  song,
});

const mockVerifiedSession = (page: Page) =>
  page.route('**/api/guest-access/secured/me', (route) =>
    fulfillJson(
      route,
      { guestId: 'guest-1', invitationId: 'invitation-1', firstName: 'Alice', lastName: 'Martin', language: 'FR' },
      200,
      CREDENTIALED_CORS,
    ),
  );

const mockSongSearch = (page: Page, results: Song[]) =>
  page.route('**/api/guest-access/secured/song-search*', (route) => fulfillJson(route, results, 200, CREDENTIALED_CORS));

const openSecuredArea = async (page: Page) => {
  await page.goto(`${PUBLIC_BASE_URL}/guest-access/secured-area`);
  await expect(page.getByText(/c.?l.?brer notre amour|celebrate our love/i)).toBeVisible({ timeout: UI_TIMEOUT_MS });
};

const submitButton = (page: Page) => page.getByRole('button', { name: /Envoyer ma r.?ponse|Send my answer/i });
const songInput = (page: Page) => page.locator('input[name="song"]');

test.describe('Guest meal & song choices', () => {
  test('lets an attending guest pick a meal and a song, and shows both after a reload', async ({ page }) => {
    await mockVerifiedSession(page);
    await mockSongSearch(page, [laVieEnRose]);

    // A stateful mock stands in for the backend: the answer submitted via POST is
    // returned by the subsequent GET, so reloading must show the meal and song persisted.
    let stored: ReturnType<typeof savedRsvp> | null = null;
    await page.route('**/api/guest-access/secured/rsvp', async (route: Route) => {
      const method = route.request().method();

      if (method === 'OPTIONS') {
        await route.fulfill({ status: 204, headers: PREFLIGHT_CORS });
        return;
      }

      if (method === 'GET') {
        if (stored === null) {
          await route.fulfill({ status: 204, headers: CREDENTIALED_CORS });
          return;
        }

        await fulfillJson(route, stored, 200, CREDENTIALED_CORS);
        return;
      }

      const body = route.request().postDataJSON();
      stored = savedRsvp(body.attendance, body.meal ?? null, body.song ?? null);
      await fulfillJson(route, stored, 200, CREDENTIALED_CORS);
    });

    await openSecuredArea(page);

    await page.locator('input[value="ATTENDING"]').check();
    await page.locator('input[value="MEAT"]').check();

    await songInput(page).fill('la vie');
    const suggestion = page.locator('.song-select', { hasText: 'La Vie en rose' });
    await expect(suggestion).toBeVisible({ timeout: UI_TIMEOUT_MS });
    await suggestion.click();

    // Selecting a song replaces the search input with the chosen-track card.
    await expect(songInput(page)).toHaveCount(0);
    await expect(page.getByText('La Vie en rose')).toBeVisible();
    await expect(page.getByText('Edith Piaf')).toBeVisible();

    await expect(submitButton(page)).toBeEnabled();
    await submitButton(page).click();
    await expect(page.getByText(/Merci du fond du c.?ur|Thank you from the bottom of our hearts/i)).toBeVisible();

    await page.reload();
    await expect(page.getByText(/c.?l.?brer notre amour|celebrate our love/i)).toBeVisible({ timeout: UI_TIMEOUT_MS });

    await expect(page.locator('input[value="ATTENDING"]')).toBeChecked();
    await expect(page.locator('input[value="MEAT"]')).toBeChecked();
    await expect(page.getByText('La Vie en rose')).toBeVisible();
    await expect(page.getByText('Edith Piaf')).toBeVisible();
  });

  test('gates the meal and song choices behind an attending answer and makes the meal mandatory', async ({ page }) => {
    await mockVerifiedSession(page);
    await page.route('**/api/guest-access/secured/rsvp', async (route: Route) => {
      if (route.request().method() === 'OPTIONS') {
        await route.fulfill({ status: 204, headers: PREFLIGHT_CORS });
        return;
      }

      // No saved answer yet: the empty state exposes the choice, nothing else.
      await route.fulfill({ status: 204, headers: CREDENTIALED_CORS });
    });

    await openSecuredArea(page);

    // Declining hides both the meal and the song choices.
    await page.locator('input[value="DECLINED"]').check();
    await expect(page.locator('input[name="meal"]')).toHaveCount(0);
    await expect(songInput(page)).toHaveCount(0);

    // Attending reveals the mandatory meal and the optional song.
    await page.locator('input[value="ATTENDING"]').check();
    await expect(page.locator('input[name="meal"]')).toHaveCount(3);
    await expect(songInput(page)).toBeVisible();

    // A meal is mandatory, so submitting stays blocked until one is picked (the song stays optional).
    await expect(submitButton(page)).toBeDisabled();
    await page.locator('input[value="FISH"]').check();
    await expect(submitButton(page)).toBeEnabled();
  });

  test('blocks the choices form without a valid session', async ({ page }) => {
    // Without a valid `guest_session` the backend rejects `/me` (401) and the guarded
    // choice endpoints (401/403); the UI must gate the form behind the recoverable
    // "expired or invalid" state instead of exposing the meal/song choices.
    await page.route('**/api/guest-access/secured/me', (route) => fulfillJson(route, {}, 401, CREDENTIALED_CORS));
    await page.route('**/api/guest-access/secured/rsvp', (route) => fulfillJson(route, {}, 401, CREDENTIALED_CORS));
    await page.route('**/api/guest-access/secured/song-search*', (route) => fulfillJson(route, {}, 403, CREDENTIALED_CORS));

    await page.goto(`${PUBLIC_BASE_URL}/guest-access/secured-area`);

    await expect(page.getByText(/expir.? ou invalide|expired or invalid/i)).toBeVisible({ timeout: UI_TIMEOUT_MS });
    await expect(submitButton(page)).toHaveCount(0);
    await expect(songInput(page)).toHaveCount(0);
  });
});

