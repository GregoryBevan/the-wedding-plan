import { expect, test } from '@playwright/test';
import { fulfillJson } from './fixtures/httpHelpers';

const PUBLIC_BASE_URL = 'http://127.0.0.1:4174';
const VALID_TOKEN = '957f8251-f50b-48ca-9cd1-998e71ffd2e9';

test.describe('Guest access invitation page', () => {
  test('reveals the invitation and its guests for a valid token', async ({ page }) => {
    await page.route('**/guest-access/invitations/**', async (route) => {
      await fulfillJson(route, {
        label: 'Famille Martin',
        description: 'Nous serions ravis de vous compter parmi nous.',
        guestCount: 2,
        guests: [
          { firstName: 'Alice', lastName: 'Martin' },
          { firstName: 'Bob', lastName: 'Martin' }
        ]
      });
    });

    await page.goto(`${PUBLIC_BASE_URL}/guest-access/${VALID_TOKEN}`);

    await expect(page.getByRole('heading', { name: 'Famille Martin' })).toBeVisible({ timeout: 10000 });
    await expect(page.getByText('Alice Martin')).toBeVisible();
    await expect(page.getByText('Bob Martin')).toBeVisible();
    await expect(page.getByText(/2 (invités|guests)/i)).toBeVisible();
  });

  test('shows a not-found message for an unknown token', async ({ page }) => {
    await page.route('**/guest-access/invitations/**', async (route) => {
      await fulfillJson(route, {}, 404);
    });

    await page.goto(`${PUBLIC_BASE_URL}/guest-access/${VALID_TOKEN}`);

    await expect(page.getByText(/introuvable|could not be found/i)).toBeVisible({ timeout: 10000 });
    await expect(page.getByRole('button', { name: /Réessayer|Try again/i })).toBeVisible();
  });

  test('allows switching language manually', async ({ page }) => {
    await page.goto(`${PUBLIC_BASE_URL}/`);

    await page.getByRole('button', { name: 'EN' }).click();
    await expect(page.getByRole('heading', { name: 'Private invitation' })).toBeVisible();

    await page.getByRole('button', { name: 'FR' }).click();
    await expect(page.getByRole('heading', { name: 'Invitation privée' })).toBeVisible();
  });
});

