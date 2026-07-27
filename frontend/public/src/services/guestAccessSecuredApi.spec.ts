import { afterEach, describe, expect, it, vi } from 'vitest';
import { GuestAccessSecuredApiError, fetchGuestSession, securedGuestFetch, submitRsvp } from './guestAccessSecuredApi';
import {
  clearCsrfCookie,
  expectCsrfHeader,
  getFirstRequest,
  mockFetchResponse,
  setCsrfCookie,
} from '../testFixtures/httpTestHelpers';

const securedBaseUrl = 'http://localhost:8080/api/guest-access/secured';

describe('guestAccessSecuredApi', () => {
  afterEach(() => {
    vi.restoreAllMocks();
    clearCsrfCookie();
  });

  it('performs a GET with credentials and without a CSRF header', async () => {
    const fetchMock = mockFetchResponse({ ok: true });

    await securedGuestFetch('/me');

    const [url, options] = getFirstRequest(fetchMock);
    expect(url).toBe(`${securedBaseUrl}/me`);
    expect(options).toMatchObject({ method: 'GET', credentials: 'include' });
    expect((options.headers as Headers).get('X-XSRF-TOKEN')).toBeNull();
  });

  it('adds the CSRF header on a mutating request when the cookie is present', async () => {
    setCsrfCookie();
    const fetchMock = mockFetchResponse({ ok: true });

    await securedGuestFetch('/rsvp', { method: 'post' });

    const [url, options] = getFirstRequest(fetchMock);
    expect(url).toBe(`${securedBaseUrl}/rsvp`);
    expect(options).toMatchObject({ method: 'POST', credentials: 'include' });
    expectCsrfHeader(options);
  });

  it('omits the CSRF header on a mutating request when the cookie is absent', async () => {
    const fetchMock = mockFetchResponse({ ok: true });

    await securedGuestFetch('/rsvp', { method: 'POST' });

    const [, options] = getFirstRequest(fetchMock);
    expect((options.headers as Headers).get('X-XSRF-TOKEN')).toBeNull();
  });

  it('submits the RSVP with credentials and CSRF header', async () => {
    setCsrfCookie();
    const fetchMock = mockFetchResponse({ ok: true });

    await submitRsvp();

    const [url, options] = getFirstRequest(fetchMock);
    expect(url).toBe(`${securedBaseUrl}/rsvp`);
    expect(options).toMatchObject({ method: 'POST', credentials: 'include' });
    expect((options.headers as Headers).get('Content-Type')).toBe('application/json');
    expectCsrfHeader(options);
  });

  it('throws a typed error when the RSVP submission fails', async () => {
    mockFetchResponse({ ok: false, status: 403 });

    await expect(submitRsvp()).rejects.toBeInstanceOf(GuestAccessSecuredApiError);
    await expect(submitRsvp()).rejects.toThrow('Unable to submit RSVP at the moment.');
  });

  it('resolves the guest session from the /me endpoint', async () => {
    const session = { guestId: 'guest-1', invitationId: 'invitation-1', firstName: 'Jane', lastName: 'Doe' };
    const fetchMock = mockFetchResponse({ ok: true, body: session });

    const result = await fetchGuestSession();

    const [url, options] = getFirstRequest(fetchMock);
    expect(url).toBe(`${securedBaseUrl}/me`);
    expect(options).toMatchObject({ method: 'GET', credentials: 'include' });
    expect(result).toEqual(session);
  });

  it('returns null when no valid guest session is present', async () => {
    mockFetchResponse({ ok: false, status: 401 });

    await expect(fetchGuestSession()).resolves.toBeNull();
  });

  it('returns null when the session guest no longer belongs to the invitation', async () => {
    mockFetchResponse({ ok: false, status: 403 });

    await expect(fetchGuestSession()).resolves.toBeNull();
  });

  it('throws a typed error when the guest session lookup fails unexpectedly', async () => {
    mockFetchResponse({ ok: false, status: 500 });

    await expect(fetchGuestSession()).rejects.toBeInstanceOf(GuestAccessSecuredApiError);
    await expect(fetchGuestSession()).rejects.toThrow('Unable to load the guest session.');
  });
});

