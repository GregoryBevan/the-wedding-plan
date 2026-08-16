import { expect, test } from '@playwright/test';
import { allowReadOnlySession } from './fixtures/authSetup';
import { fulfillJson } from './fixtures/httpHelpers';
import { NAVIGATION_TIMEOUT_MS, UI_TIMEOUT_MS } from './fixtures/timeouts';

test.describe('Guests read-only access', () => {
  test('read-only user browses guests without write controls', async ({ page }) => {
    await allowReadOnlySession(page);

    await page.route('**/api/guests*', async (route) => {
      await fulfillJson(route, {
        items: [
          {
            id: '1',
            version: 1,
            creationDate: '2026-06-25T00:00:00Z',
            updateDate: '2026-06-25T00:00:00Z',
            firstName: 'Jane',
            lastName: 'Doe',
            email: 'jane.doe@email.com',
            language: 'EN'
          }
        ],
        page: 0,
        size: 10,
        totalItems: 1,
        totalPages: 1
      });
    });

    const guestsLoaded = page.waitForResponse((response) =>
      response.url().includes('/api/guests') && response.request().method() === 'GET'
    );

    await page.goto('/guests');
    await guestsLoaded;

    await expect(page.getByText('jane.doe@email.com')).toBeVisible({ timeout: UI_TIMEOUT_MS });
    await expect(page.locator('[data-test="add-guest-shortcut"]')).toHaveCount(0);
    await expect(page.locator('[data-test="edit-guest-1"]')).toHaveCount(0);
    await expect(page.locator('[data-test="archive-guest-1"]')).toHaveCount(0);
  });

  test('read-only user is redirected from guest write route to access denied', async ({ page }) => {
    await allowReadOnlySession(page);

    await page.goto('/guests/new');

    await expect(page).toHaveURL(/\/access-denied$/, { timeout: NAVIGATION_TIMEOUT_MS });
    await expect(page.getByText('Your account is not authorized to access this backoffice.')).toBeVisible({ timeout: UI_TIMEOUT_MS });
  });

  test('read-only user opens guest details without the edit control', async ({ page }) => {
    await allowReadOnlySession(page);

    await page.route('**/api/guests/*', async (route) => {
      await fulfillJson(route, {
        id: '1',
        version: 1,
        creationDate: '2026-06-25T00:00:00Z',
        updateDate: '2026-06-25T00:00:00Z',
        firstName: 'Jane',
        lastName: 'Doe',
        email: 'jane.doe@email.com',
        language: 'EN'
      });
    });

    const guestLoaded = page.waitForResponse((response) =>
      response.url().includes('/api/guests/1') && response.request().method() === 'GET'
    );

    await page.goto('/guests/1');
    await guestLoaded;

    await expect(page.locator('[data-test="guest-details-card"]')).toBeVisible({ timeout: UI_TIMEOUT_MS });
    await expect(page.locator('[data-test="guest-details-email"]')).toHaveText('jane.doe@email.com');
    await expect(page.locator('[data-test="guest-details-language"]')).toHaveText('English');
    await expect(page.locator('[data-test="edit-guest-link"]')).toHaveCount(0);
  });
});

