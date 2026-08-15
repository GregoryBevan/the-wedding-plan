import type { Page } from '@playwright/test';

export const allowAdminSession = async (page: Page) => {
  await page.route('**/auth/me', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        authenticated: true,
        authorized: true,
        canWrite: true,
        email: 'planner@example.com'
      })
    });
  });
};

export const allowReadOnlySession = async (page: Page) => {
  await page.route('**/auth/me', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        authenticated: true,
        authorized: true,
        canWrite: false,
        email: 'viewer@example.com'
      })
    });
  });
};

