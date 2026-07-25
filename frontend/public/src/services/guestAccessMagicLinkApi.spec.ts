import { afterEach, describe, expect, it, vi } from 'vitest';
import { GuestAccessMagicLinkApiError, requestMagicLink } from './guestAccessMagicLinkApi';
import {
  clearCsrfCookie,
  expectCsrfHeader,
  getFirstRequest,
  mockFetchResponse,
  setCsrfCookie,
} from '../testFixtures/httpTestHelpers';

const magicLinkRequestUrl =
  'http://localhost:8080/api/guest-access/invitations/a%20b%2Fc/guests/guest-1/magic-link-requests';

describe('guestAccessMagicLinkApi', () => {
  afterEach(() => {
    vi.restoreAllMocks();
    clearCsrfCookie();
  });

  it('requests a magic link with credentials and the CSRF header', async () => {
    setCsrfCookie();
    const fetchMock = mockFetchResponse({ ok: true, status: 202 });

    const result = await requestMagicLink('a b/c', 'guest-1');

    const [url, options] = getFirstRequest(fetchMock);
    expect(url).toBe(magicLinkRequestUrl);
    expect(options).toMatchObject({ method: 'POST', credentials: 'include' });
    expectCsrfHeader(options);
    expect(result).toEqual({ status: 'accepted' });
  });

  it('reports a rate-limited result with the Retry-After delay', async () => {
    mockFetchResponse({ ok: false, status: 429, headers: { 'Retry-After': '42' } });

    const result = await requestMagicLink('token', 'guest-1');

    expect(result).toEqual({ status: 'rateLimited', retryAfterSeconds: 42 });
  });

  it('reports a rate-limited result with a null delay when Retry-After is missing', async () => {
    mockFetchResponse({ ok: false, status: 429 });

    const result = await requestMagicLink('token', 'guest-1');

    expect(result).toEqual({ status: 'rateLimited', retryAfterSeconds: null });
  });

  it('throws a typed error when the request fails', async () => {
    mockFetchResponse({ ok: false, status: 400 });

    await expect(requestMagicLink('token', 'guest-1')).rejects.toBeInstanceOf(GuestAccessMagicLinkApiError);
    await expect(requestMagicLink('token', 'guest-1')).rejects.toThrow('Unable to request the magic link.');
  });
});

