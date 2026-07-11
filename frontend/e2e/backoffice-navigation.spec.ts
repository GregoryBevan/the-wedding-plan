import { expect, test } from '@playwright/test';

const allowAuthorizedSession = async (page: import('@playwright/test').Page) => {
  await page.route('**/auth/me', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        isAuthenticated: true,
        isAuthorized: true,
        email: 'planner@example.com'
      })
    });
  });
};

test.describe('Backoffice navigation', () => {
  test('add guest view shows success toast and returns to list after submit', async ({ page }) => {
    await allowAuthorizedSession(page);

    await page.route('**/api/guests', async (route) => {
      if (route.request().method() === 'POST') {
        await route.fulfill({
          status: 201,
          contentType: 'application/json',
          body: JSON.stringify({
            id: 'new-guest',
            version: 1,
            creationDate: '2026-06-25T00:00:00Z',
            updateDate: '2026-06-25T00:00:00Z',
            firstName: 'John',
            lastName: 'Doe',
            email: 'john.doe@email.com'
          })
        });
        return;
      }

      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          items: [],
          page: 2,
          size: 10,
          totalItems: 0,
          totalPages: 1
        })
      });
    });

    await page.goto('/guests/new?page=2&size=10');

    await page.getByLabel('First Name').fill('John');
    await page.getByLabel('Last Name').fill('Doe');
    await page.getByLabel('Email').fill('john.doe@email.com');
    await page.getByRole('button', { name: 'Add Guest' }).click();

    await expect(page.getByText('Guest added successfully.')).toBeVisible();


    await expect(page).toHaveURL(/\/guests\?page=2&size=10$/);
  });

  test('edit flow keeps list context when canceling', async ({ page }) => {
    await allowAuthorizedSession(page);

    await page.route('**/api/guests**', async (route) => {
      const requestUrl = new URL(route.request().url());
      const pageParam = requestUrl.searchParams.get('page');
      const sizeParam = requestUrl.searchParams.get('size');

      if (requestUrl.pathname !== '/api/guests' || pageParam !== '2' || sizeParam !== '10') {
        await route.fallback();
        return;
      }

      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          items: [
            {
              id: '1',
              version: 1,
              creationDate: '2026-06-25T00:00:00Z',
              updateDate: '2026-06-25T00:00:00Z',
              firstName: 'Jane',
              lastName: 'Doe',
              email: 'jane.doe@email.com'
            }
          ],
          page: 2,
          size: 10,
          totalItems: 1,
          totalPages: 1
        })
      });
    });

    await page.route('**/api/guests/1', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          id: '1',
          version: 1,
          creationDate: '2026-06-25T00:00:00Z',
          updateDate: '2026-06-25T00:00:00Z',
          firstName: 'Jane',
          lastName: 'Doe',
          email: 'jane.doe@email.com'
        })
      });
    });

    await page.goto('/guests?page=2&size=10');
    await page.locator('[data-test="edit-guest-1"]').click();

    await expect(page.getByRole('heading', { name: 'Edit Guest' })).toBeVisible();
    await page.getByRole('button', { name: 'Cancel' }).first().click();

    await expect(page).toHaveURL(/\/guests\?page=2&size=10$/);
  });
});

test.describe('Unsaved changes confirmation dialog', () => {
  test('shows custom confirm dialog when canceling form with unsaved changes', async ({ page }) => {
    await allowAuthorizedSession(page);

    await page.goto('/guests/new');

    await page.getByLabel('First Name').fill('John');

    await page.getByRole('button', { name: 'Cancel' }).first().click();

    const dialogTitle = page.getByRole('heading', { name: 'Unsaved Changes' });
    const dialogMessage = page.getByText('You have unsaved guest details');
    const leaveButton = page.getByRole('button', { name: 'Leave' });
    const continueButton = page.getByRole('button', { name: 'Continue editing' });

    await expect(dialogTitle).toBeVisible();
    await expect(dialogMessage).toBeVisible();
    await expect(leaveButton).toBeVisible();
    await expect(continueButton).toBeVisible();
  });

  test('continues editing when clicking Continue button in dialog', async ({ page }) => {
    await allowAuthorizedSession(page);

    await page.goto('/guests/new');

    await page.getByLabel('First Name').fill('John');
    await page.getByRole('button', { name: 'Cancel' }).first().click();
    await page.getByRole('button', { name: 'Continue editing' }).click();

    await expect(page).toHaveURL('/guests/new');
    await expect(page.getByLabel('First Name')).toHaveValue('John');
  });

  test('leaves form and navigates when clicking Leave button in dialog', async ({ page }) => {
    await allowAuthorizedSession(page);

    await page.route('**/api/guests', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          items: [],
          page: 0,
          size: 10,
          totalItems: 0,
          totalPages: 1
        })
      });
    });

    await page.goto('/guests/new');

    await page.getByLabel('First Name').fill('John');
    await page.getByRole('button', { name: 'Cancel' }).first().click();
    await page.getByRole('button', { name: 'Leave' }).click();

    await expect(page).toHaveURL(/\/guests/);
  });
});


