import type { Page } from '@playwright/test';

export const allowAuthorizedSession = async (page: Page) => {
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

