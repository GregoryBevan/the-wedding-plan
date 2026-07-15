import { expect, test } from '@playwright/test';
import { allowAuthorizedSession } from './fixtures/authSetup';
import { fulfillJson } from './fixtures/httpHelpers';

test.describe('Invitation edit', () => {
  test('edit invitation successfully and redirect to details', async ({ page }) => {
    await allowAuthorizedSession(page);

    const invitationId = 'inv-1';

    // Mock API calls for editing invitation
    await page.route('**/api/invitations/inv-1', async (route) => {
      if (route.request().method() === 'GET') {
        await fulfillJson(route, {
          id: invitationId,
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
            },
            {
              id: 'guest-2',
              firstName: 'Bob',
              lastName: 'Durand',
              email: 'bob@example.com'
            }
          ],
          guestCount: 2
        });
      } else if (route.request().method() === 'PUT') {
        await fulfillJson(route, {
          id: invitationId,
          version: 1,
          creationDate: '2026-07-03T10:00:00Z',
          updateDate: '2026-07-04T10:00:00Z',
          label: 'Family table updated',
          description: 'Updated family table',
          guests: [
            {
              id: 'guest-1',
              firstName: 'Alice',
              lastName: 'Martin',
              email: 'alice@example.com'
            }
          ],
          guestCount: 1
        });
      } else {
        await route.fallback();
      }
    });

    // Mock guests API
    await page.route('**/api/guests*', async (route) => {
      await fulfillJson(route, {
        items: [
          {
            id: 'guest-1',
            version: 1,
            creationDate: '2026-06-25T00:00:00Z',
            updateDate: '2026-06-25T00:00:00Z',
            firstName: 'Alice',
            lastName: 'Martin',
            email: 'alice@example.com'
          },
          {
            id: 'guest-2',
            version: 1,
            creationDate: '2026-06-25T00:00:00Z',
            updateDate: '2026-06-25T00:00:00Z',
            firstName: 'Bob',
            lastName: 'Durand',
            email: 'bob@example.com'
          }
        ],
        page: 0,
        size: 20,
        totalItems: 2,
        totalPages: 1
      });
    });

    await page.goto(`/invitations/${invitationId}/edit`);

    await expect(page.getByRole('heading', { name: 'Edit invitation' })).toBeVisible({ timeout: 10000 });

    const labelInput = page.locator('input[id="invitation-label"]');
    const descriptionInput = page.locator('textarea[id="invitation-description"]');

    await expect(labelInput).toHaveValue('Family table', { timeout: 10000 });
    await expect(descriptionInput).toHaveValue('Main family table');

    await labelInput.fill('Family table updated');
    await descriptionInput.fill('Updated family table');

    // Uncheck the second guest
    const checkboxes = page.locator('input[type="checkbox"]');
    await checkboxes.nth(1).uncheck();

    await page.getByRole('button', { name: /Update invitation/ }).click();

    await expect(page).toHaveURL(`/invitations/${invitationId}`, { timeout: 10000 });
  });

  test('shows validation error when label is empty', async ({ page }) => {
    await allowAuthorizedSession(page);

    const invitationId = 'inv-2';

    await page.route('**/api/invitations/inv-2', async (route) => {
      await fulfillJson(route, {
        id: invitationId,
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
      });
    });

    await page.route('**/api/guests*', async (route) => {
      await fulfillJson(route, {
        items: [
          {
            id: 'guest-1',
            version: 1,
            creationDate: '2026-06-25T00:00:00Z',
            updateDate: '2026-06-25T00:00:00Z',
            firstName: 'Alice',
            lastName: 'Martin',
            email: 'alice@example.com'
          }
        ],
        page: 0,
        size: 20,
        totalItems: 1,
        totalPages: 1
      });
    });

    await page.goto(`/invitations/${invitationId}/edit`);

    const labelInput = page.locator('input[id="invitation-label"]');
    await labelInput.fill('   ', { timeout: 10000 });

    await page.getByRole('button', { name: /Update invitation/ }).click();

    await expect(page.locator('[data-test="invitation-validation-error"]')).toContainText('Label is required.', { timeout: 5000 });
    await expect(page).toHaveURL(`/invitations/${invitationId}/edit`);
  });

  test('loads selectable guests using unassigned availability filter', async ({ page }) => {
    await allowAuthorizedSession(page);

    const invitationId = 'inv-unassigned-filter';
    let guestAvailability: string | null = null;

    await page.route('**/api/invitations/inv-unassigned-filter', async (route) => {
      await fulfillJson(route, {
        id: invitationId,
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
      });
    });

    await page.route('**/api/guests*', async (route) => {
      const requestUrl = new URL(route.request().url());
      guestAvailability = requestUrl.searchParams.get('availability');

      await fulfillJson(route, {
        items: [
          {
            id: 'guest-1',
            version: 1,
            creationDate: '2026-06-25T00:00:00Z',
            updateDate: '2026-06-25T00:00:00Z',
            firstName: 'Alice',
            lastName: 'Martin',
            email: 'alice@example.com'
          }
        ],
        page: 0,
        size: 20,
        totalItems: 1,
        totalPages: 1
      });
    });

    await page.goto(`/invitations/${invitationId}/edit`);

    await expect(page.getByRole('heading', { name: 'Edit invitation' })).toBeVisible({ timeout: 10000 });
    await expect.poll(() => guestAvailability).toBe('unassigned');
  });

  test('shows validation error when no guests are selected', async ({ page }) => {
    await allowAuthorizedSession(page);

    const invitationId = 'inv-3';

    await page.route('**/api/invitations/inv-3', async (route) => {
      await fulfillJson(route, {
        id: invitationId,
        version: 1,
        creationDate: '2026-07-03T10:00:00Z',
        updateDate: '2026-07-03T10:00:00Z',
        label: 'Family table',
        description: 'Main family table',
        guests: [
          {
            id: 'guest-1',
            version: 1,
            firstName: 'Alice',
            lastName: 'Martin',
            email: 'alice@example.com'
          }
        ],
        guestCount: 1
      });
    });

    await page.route('**/api/guests*', async (route) => {
      await fulfillJson(route, {
        items: [
          {
            id: 'guest-1',
            version: 1,
            creationDate: '2026-06-25T00:00:00Z',
            updateDate: '2026-06-25T00:00:00Z',
            firstName: 'Alice',
            lastName: 'Martin',
            email: 'alice@example.com'
          }
        ],
        page: 0,
        size: 20,
        totalItems: 1,
        totalPages: 1
      });
    });

    await page.goto(`/invitations/${invitationId}/edit`);

    await page.locator('input[type="checkbox"]').first().uncheck({ timeout: 10000 });

    await page.getByRole('button', { name: /Update invitation/ }).click();

    await expect(page.locator('[data-test="invitation-validation-error"]')).toContainText('Select at least one guest.', { timeout: 5000 });
    await expect(page).toHaveURL(`/invitations/${invitationId}/edit`);
  });

  test('shows error when invitation not found', async ({ page }) => {
    await allowAuthorizedSession(page);

    const invitationId = 'inv-404';

    await page.route('**/api/invitations/inv-404', async (route) => {
      await fulfillJson(route, {}, 404);
    });

    await page.goto(`/invitations/${invitationId}/edit`);

    await expect(page.locator('[data-test="invitation-load-error"]')).toContainText('Invitation not found.', { timeout: 10000 });
  });

  test('cancel button returns to invitation details', async ({ page }) => {
    await allowAuthorizedSession(page);

    const invitationId = 'inv-5';

    await page.route('**/api/invitations/inv-5', async (route) => {
      await fulfillJson(route, {
        id: invitationId,
        version: 1,
        creationDate: '2026-07-03T10:00:00Z',
        updateDate: '2026-07-03T10:00:00Z',
        label: 'Family table',
        description: 'Main family table',
        guests: [
          {
            id: 'guest-1',
            version: 1,
            firstName: 'Alice',
            lastName: 'Martin',
            email: 'alice@example.com'
          }
        ],
        guestCount: 1
      });
    });

    await page.route('**/api/guests*', async (route) => {
      await fulfillJson(route, {
        items: [
          {
            id: 'guest-1',
            version: 1,
            creationDate: '2026-06-25T00:00:00Z',
            updateDate: '2026-06-25T00:00:00Z',
            firstName: 'Alice',
            lastName: 'Martin',
            email: 'alice@example.com'
          }
        ],
        page: 0,
        size: 20,
        totalItems: 1,
        totalPages: 1
      });
    });

    await page.goto(`/invitations/${invitationId}/edit`);

    await expect(page.getByRole('heading', { name: 'Edit invitation' })).toBeVisible({ timeout: 10000 });

    const cancelLink = page.locator('a').filter({ hasText: 'Cancel' }).first();
    await cancelLink.click();

    await expect(page).toHaveURL(`/invitations/${invitationId}`, { timeout: 10000 });
  });
});


