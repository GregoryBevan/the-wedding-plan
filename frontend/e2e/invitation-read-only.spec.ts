import { expect, test } from '@playwright/test';
import { allowReadOnlySession } from './fixtures/authSetup';
import { fulfillJson } from './fixtures/httpHelpers';
import { NAVIGATION_TIMEOUT_MS, UI_TIMEOUT_MS } from './fixtures/timeouts';

test.describe('Invitations read-only access', () => {
  test('read-only user browses invitations without write controls', async ({ page }) => {
    await allowReadOnlySession(page);

    await page.route('**/api/invitations*', async (route) => {
      await fulfillJson(route, {
        items: [
          {
            id: 'inv-1',
            accessToken: 'token-inv-1',
            version: 1,
            creationDate: '2026-07-03T10:00:00Z',
            updateDate: '2026-07-03T10:00:00Z',
            label: 'Family table',
            description: 'Main family table',
            guests: [
              {
                id: 'guest-1',
                firstName: 'Alice',
                lastName: 'Martin',
                email: 'alice@example.com'
              }
            ],
            guestCount: 1
          }
        ],
        page: 0,
        size: 20,
        totalItems: 1,
        totalPages: 1
      });
    });

    const invitationsLoaded = page.waitForResponse((response) =>
      response.url().includes('/api/invitations') && response.request().method() === 'GET'
    );

    await page.goto('/invitations');
    await invitationsLoaded;

    await expect(page.locator('[data-test="invitation-card"]')).toBeVisible({ timeout: UI_TIMEOUT_MS });
    await expect(page.locator('[data-test="create-invitation-cta"]')).toHaveCount(0);
    await expect(page.locator('a[aria-label="View invitation"]')).toBeVisible();
    await expect(page.locator('a[aria-label="Edit invitation"]')).toHaveCount(0);
  });

  test('read-only user is redirected from invitation write route to access denied', async ({ page }) => {
    await allowReadOnlySession(page);

    await page.goto('/invitations/new');

    await expect(page).toHaveURL(/\/access-denied$/, { timeout: NAVIGATION_TIMEOUT_MS });
    await expect(page.getByText('Your account is not authorized to access this backoffice.')).toBeVisible({ timeout: UI_TIMEOUT_MS });
  });
});





