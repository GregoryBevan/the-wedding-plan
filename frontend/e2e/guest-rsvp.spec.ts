import { expect, test, type Page, type Route } from '@playwright/test';
import { fulfillJson } from './fixtures/httpHelpers';
import { UI_TIMEOUT_MS } from './fixtures/timeouts';

const PUBLIC_BASE_URL = 'http://127.0.0.1:4174';

// The `/me` and `/rsvp` requests are sent with `credentials: 'include'`, so the
// browser requires the exact origin to be echoed back with `allow-credentials`.
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

const savedRsvp = (attendance: Attendance, meal: Meal | null = null) => ({
  id: 'rsvp-1',
  version: 1,
  creationDate: '2026-06-13T10:00:00',
  updateDate: '2026-06-13T10:00:00',
  attendance,
  meal,
});

const mockVerifiedSession = (page: Page) =>
  page.route('**/api/guest-access/secured/me', async (route) => {
    await fulfillJson(
      route,
      { guestId: 'guest-1', invitationId: 'invitation-1', firstName: 'Alice', lastName: 'Martin', language: 'FR' },
      200,
      CREDENTIALED_CORS,
    );
  });

const openSecuredArea = async (page: Page) => {
  await page.goto(`${PUBLIC_BASE_URL}/guest-access/secured-area`);
  await expect(page.getByText(/célébrer notre amour|celebrate our love/i)).toBeVisible({ timeout: UI_TIMEOUT_MS });
};

const submitButton = (page: Page) =>
  page.getByRole('button', { name: /Envoyer ma réponse|Send my answer/i });

test.describe('Guest RSVP form', () => {
  test('submits attendance from an empty state and shows the saved confirmation', async ({ page }) => {
    await mockVerifiedSession(page);
    await page.route('**/api/guest-access/secured/rsvp', async (route: Route) => {
      const method = route.request().method();

      if (method === 'OPTIONS') {
        await route.fulfill({ status: 204, headers: PREFLIGHT_CORS });
        return;
      }

      if (method === 'GET') {
        await route.fulfill({ status: 204, headers: CREDENTIALED_CORS });
        return;
      }

      await fulfillJson(route, savedRsvp('ATTENDING', 'MEAT'), 200, CREDENTIALED_CORS);
    });

    await openSecuredArea(page);

    await expect(submitButton(page)).toBeDisabled();
    await page.locator('input[value="ATTENDING"]').check();
    // A meal is mandatory to attend, so the submit stays disabled until one is picked.
    await expect(submitButton(page)).toBeDisabled();
    await page.locator('input[value="MEAT"]').check();
    await expect(submitButton(page)).toBeEnabled();

    await submitButton(page).click();

    await expect(page.getByText(/Merci du fond du cœur|Thank you from the bottom of our hearts/i)).toBeVisible();
  });

  test('prefills an existing response and lets the guest change it', async ({ page }) => {
    await mockVerifiedSession(page);
    await page.route('**/api/guest-access/secured/rsvp', async (route: Route) => {
      const method = route.request().method();

      if (method === 'OPTIONS') {
        await route.fulfill({ status: 204, headers: PREFLIGHT_CORS });
        return;
      }

      if (method === 'GET') {
        await fulfillJson(route, savedRsvp('ATTENDING', 'FISH'), 200, CREDENTIALED_CORS);
        return;
      }

      await fulfillJson(route, savedRsvp('DECLINED'), 200, CREDENTIALED_CORS);
    });

    await openSecuredArea(page);

    await expect(page.locator('input[value="ATTENDING"]')).toBeChecked();
    await expect(submitButton(page)).toBeDisabled();

    await page.locator('input[value="DECLINED"]').check();
    await expect(submitButton(page)).toBeEnabled();

    await submitButton(page).click();

    await expect(page.getByText(/Merci du fond du cœur|Thank you from the bottom of our hearts/i)).toBeVisible();
  });

  test('surfaces a retriable error when the submission fails', async ({ page }) => {
    await mockVerifiedSession(page);

    let postAttempts = 0;
    await page.route('**/api/guest-access/secured/rsvp', async (route: Route) => {
      const method = route.request().method();

      if (method === 'OPTIONS') {
        await route.fulfill({ status: 204, headers: PREFLIGHT_CORS });
        return;
      }

      if (method === 'GET') {
        await route.fulfill({ status: 204, headers: CREDENTIALED_CORS });
        return;
      }

      postAttempts += 1;
      if (postAttempts === 1) {
        await fulfillJson(route, {}, 500, CREDENTIALED_CORS);
        return;
      }

      await fulfillJson(route, savedRsvp('ATTENDING', 'MEAT'), 200, CREDENTIALED_CORS);
    });

    await openSecuredArea(page);

    await page.locator('input[value="ATTENDING"]').check();
    await page.locator('input[value="MEAT"]').check();
    await submitButton(page).click();

    await expect(page.getByText(/votre réponse n'a pas été enregistrée|couldn't save your answer/i)).toBeVisible();

    await submitButton(page).click();

    await expect(page.getByText(/Merci du fond du cœur|Thank you from the bottom of our hearts/i)).toBeVisible();
  });

  test('persists the submitted attendance across a reload', async ({ page }) => {
    await mockVerifiedSession(page);

    // A stateful mock stands in for the backend: the answer submitted via POST is
    // returned by the subsequent GET, so reloading the page must show it persisted.
    let storedAttendance: Attendance | null = null;
    await page.route('**/api/guest-access/secured/rsvp', async (route: Route) => {
      const method = route.request().method();

      if (method === 'OPTIONS') {
        await route.fulfill({ status: 204, headers: PREFLIGHT_CORS });
        return;
      }

      if (method === 'GET') {
        if (storedAttendance === null) {
          await route.fulfill({ status: 204, headers: CREDENTIALED_CORS });
          return;
        }

        await fulfillJson(route, savedRsvp(storedAttendance), 200, CREDENTIALED_CORS);
        return;
      }

      storedAttendance = 'DECLINED';
      await fulfillJson(route, savedRsvp(storedAttendance), 200, CREDENTIALED_CORS);
    });

    await openSecuredArea(page);

    await page.locator('input[value="DECLINED"]').check();
    await submitButton(page).click();
    await expect(page.getByText(/Merci du fond du cœur|Thank you from the bottom of our hearts/i)).toBeVisible();

    await page.reload();
    await expect(page.getByText(/célébrer notre amour|celebrate our love/i)).toBeVisible({ timeout: UI_TIMEOUT_MS });

    await expect(page.locator('input[value="DECLINED"]')).toBeChecked();
    await expect(submitButton(page)).toBeDisabled();
  });

  test('blocks the RSVP form when the session is not valid', async ({ page }) => {
    // Without a valid `guest_session` the backend rejects `/me` (401) and the
    // guarded `/rsvp` endpoint (401/403); the UI must gate the form behind the
    // recoverable "expired or invalid" state instead of exposing the RSVP.
    await page.route('**/api/guest-access/secured/me', async (route) => {
      await fulfillJson(route, {}, 401, CREDENTIALED_CORS);
    });
    await page.route('**/api/guest-access/secured/rsvp', async (route) => {
      await fulfillJson(route, {}, 401, CREDENTIALED_CORS);
    });

    await page.goto(`${PUBLIC_BASE_URL}/guest-access/secured-area`);

    await expect(page.getByText(/expiré ou invalide|expired or invalid/i)).toBeVisible({ timeout: UI_TIMEOUT_MS });
    await expect(page.getByText(/célébrer notre amour|celebrate our love/i)).toHaveCount(0);
    await expect(submitButton(page)).toHaveCount(0);
  });
});



