import { afterEach, describe, expect, it, vi } from 'vitest';
import { GuestAccessSecuredApiError, fetchGuestSession, fetchRsvp, securedGuestFetch, submitRsvp } from './guestAccessSecuredApi';
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

  it('submits a declined RSVP with credentials, payload and CSRF header', async () => {
    setCsrfCookie();
    const saved = {
      id: 'rsvp-1',
      version: 1,
      creationDate: '2026-06-13T10:00:00',
      updateDate: '2026-06-13T10:00:00',
      attendance: 'DECLINED',
    };
    const fetchMock = mockFetchResponse({ ok: true, body: saved });

    const result = await submitRsvp('DECLINED');

    const [url, options] = getFirstRequest(fetchMock);
    expect(url).toBe(`${securedBaseUrl}/rsvp`);
    expect(options).toMatchObject({ method: 'POST', credentials: 'include' });
    expect((options.headers as Headers).get('Content-Type')).toBe('application/json');
    expect(options.body).toBe(JSON.stringify({ attendance: 'DECLINED' }));
    expectCsrfHeader(options);
    expect(result).toEqual(saved);
  });

  it('submits an attending RSVP with the chosen meal in the payload', async () => {
    setCsrfCookie();
    const saved = {
      id: 'rsvp-1',
      version: 1,
      creationDate: '2026-06-13T10:00:00',
      updateDate: '2026-06-13T10:00:00',
      attendance: 'ATTENDING',
      meal: 'FISH',
    };
    const fetchMock = mockFetchResponse({ ok: true, body: saved });

    const result = await submitRsvp('ATTENDING', 'FISH');

    const [, options] = getFirstRequest(fetchMock);
    expect(options.body).toBe(JSON.stringify({ attendance: 'ATTENDING', meal: 'FISH' }));
    expect(result).toEqual(saved);
  });

  it('throws a typed error when the RSVP submission fails', async () => {
    mockFetchResponse({ ok: false, status: 403 });

    await expect(submitRsvp('ATTENDING', 'MEAT')).rejects.toBeInstanceOf(GuestAccessSecuredApiError);
    await expect(submitRsvp('ATTENDING', 'MEAT')).rejects.toThrow('Unable to submit RSVP at the moment.');
  });

  it('loads the current guest RSVP', async () => {
    const rsvp = {
      id: 'rsvp-1',
      version: 2,
      creationDate: '2026-06-13T10:00:00',
      updateDate: '2026-06-14T10:00:00',
      attendance: 'DECLINED',
    };
    const fetchMock = mockFetchResponse({ ok: true, body: rsvp });

    const result = await fetchRsvp();

    const [url, options] = getFirstRequest(fetchMock);
    expect(url).toBe(`${securedBaseUrl}/rsvp`);
    expect(options).toMatchObject({ method: 'GET', credentials: 'include' });
    expect(result).toEqual(rsvp);
  });

  it('returns null when the guest has not responded yet', async () => {
    mockFetchResponse({ ok: true, status: 204 });

    await expect(fetchRsvp()).resolves.toBeNull();
  });

  it('throws a typed error when the RSVP lookup fails', async () => {
    mockFetchResponse({ ok: false, status: 500 });

    await expect(fetchRsvp()).rejects.toBeInstanceOf(GuestAccessSecuredApiError);
    await expect(fetchRsvp()).rejects.toThrow('Unable to load your RSVP at the moment.');
  });

  it('resolves the guest session from the /me endpoint', async () => {
    const session = { guestId: 'guest-1', invitationId: 'invitation-1', firstName: 'Jane', lastName: 'Doe', language: 'FR' };
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

