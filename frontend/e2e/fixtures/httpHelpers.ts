import type { Route } from '@playwright/test';

export const fulfillJson = async (
  route: Route,
  body: unknown,
  status = 200,
  headers?: Record<string, string>
) => {
  await route.fulfill({
    status,
    contentType: 'application/json',
    headers,
    body: JSON.stringify(body)
  });
};

