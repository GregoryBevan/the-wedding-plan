import { afterEach, describe, expect, it, vi } from 'vitest';
import { GuestAccessSecuredApiError, fetchGuestSession, fetchRsvp, searchSongs, securedGuestFetch, submitRsvp } from './guestAccessSecuredApi';
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

  it('submits an attending RSVP with the chosen meal and song in the payload', async () => {
    setCsrfCookie();
    const song = {
      deezerId: 3135556,
      title: 'La Vie en rose',
      artist: 'Édith Piaf',
      link: 'https://www.deezer.com/track/3135556',
      preview: 'https://cdns-preview.deezer.com/stream/la-vie-en-rose.mp3',
    };
    const saved = {
      id: 'rsvp-1',
      version: 1,
      creationDate: '2026-06-13T10:00:00',
      updateDate: '2026-06-13T10:00:00',
      attendance: 'ATTENDING',
      meal: 'FISH',
      song,
    };
    const fetchMock = mockFetchResponse({ ok: true, body: saved });

    const result = await submitRsvp('ATTENDING', 'FISH', song);

    const [, options] = getFirstRequest(fetchMock);
    expect(options.body).toBe(JSON.stringify({ attendance: 'ATTENDING', meal: 'FISH', song }));
    expect(result).toEqual(saved);
  });

  it('sends an explicit null song to clear a previously saved one', async () => {
    setCsrfCookie();
    const saved = {
      id: 'rsvp-1',
      version: 2,
      creationDate: '2026-06-13T10:00:00',
      updateDate: '2026-06-13T11:00:00',
      attendance: 'ATTENDING',
      meal: 'FISH',
    };
    const fetchMock = mockFetchResponse({ ok: true, body: saved });

    const result = await submitRsvp('ATTENDING', 'FISH', null);

    const [, options] = getFirstRequest(fetchMock);
    expect(options.body).toBe(JSON.stringify({ attendance: 'ATTENDING', meal: 'FISH', song: null }));
    expect(result).toEqual(saved);
  });

  it('throws a typed error when the RSVP submission fails', async () => {
    mockFetchResponse({ ok: false, status: 403 });

    await expect(submitRsvp('ATTENDING', 'MEAT')).rejects.toBeInstanceOf(GuestAccessSecuredApiError);
    await expect(submitRsvp('ATTENDING', 'MEAT')).rejects.toThrow('Unable to submit RSVP at the moment.');
  });

  it('searches songs with the query encoded and returns the suggestions', async () => {
    const suggestions = [
      {
        deezerId: 3135556,
        title: 'La Vie en rose',
        artist: 'Édith Piaf',
        link: 'https://www.deezer.com/track/3135556',
        preview: 'https://cdns-preview.deezer.com/stream/la-vie-en-rose.mp3',
      },
    ];
    const fetchMock = mockFetchResponse({ ok: true, body: suggestions });

    const result = await searchSongs('la vie & rose');

    const [url, options] = getFirstRequest(fetchMock);
    expect(url).toBe(`${securedBaseUrl}/song-search?q=la%20vie%20%26%20rose`);
    expect(options).toMatchObject({ method: 'GET', credentials: 'include' });
    expect(result).toEqual(suggestions);
  });

  it('throws a typed error when the song search fails', async () => {
    mockFetchResponse({ ok: false, status: 502 });

    await expect(searchSongs('piaf')).rejects.toBeInstanceOf(GuestAccessSecuredApiError);
    await expect(searchSongs('piaf')).rejects.toThrow('Unable to search songs at the moment.');
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

