import { afterEach, describe, expect, it, vi } from 'vitest';
import { addGuest, archiveGuest, getGuestById, listGuests, restoreGuest, updateGuest } from './guestApi';
import { createGuestPage, createGuestPayload, createGuestResponse } from '../testFixtures/guestFixtures';
import { clearCsrfCookie, expectCsrfHeader, getFirstRequest, mockFetchResponse, setCsrfCookie } from '../testFixtures/httpTestHelpers';

const guestNotFoundMessage = 'Guest not found.';

describe('guestApi', () => {
  afterEach(() => {
    vi.restoreAllMocks();
    clearCsrfCookie();
  });

  it('calls backend list endpoint with default pagination', async () => {
    const fetchMock = mockFetchResponse({
      ok: true,
      body: createGuestPage({
        items: [],
        totalItems: 0,
        totalPages: 0
      })
    });

    await listGuests();

    expect(fetchMock).toHaveBeenCalledTimes(1);
    const [url, options] = getFirstRequest(fetchMock);

    expect(url).toBe('http://localhost:8080/api/guests?page=0&size=20&status=active&availability=all');
    expect(options).toMatchObject({
      method: 'GET',
      credentials: 'include'
    });
  });

  it('throws when list endpoint response is not ok', async () => {
    mockFetchResponse({ ok: false });

    await expect(listGuests()).rejects.toThrow('Unable to retrieve guests at the moment.');
  });

  it('calls backend list endpoint with archived status', async () => {
    const fetchMock = mockFetchResponse({ ok: true, body: createGuestPage() });

    await listGuests({ page: 1, size: 50, status: 'archived' });

    const [url] = getFirstRequest(fetchMock);
    expect(url).toBe('http://localhost:8080/api/guests?page=1&size=50&status=archived&availability=all');
  });

  it('calls backend list endpoint with search query', async () => {
    const fetchMock = mockFetchResponse({ ok: true, body: createGuestPage() });

    await listGuests({ page: 0, size: 20, status: 'active', search: 'john doe' });

    const [url] = getFirstRequest(fetchMock);
    expect(url).toBe('http://localhost:8080/api/guests?page=0&size=20&status=active&availability=all&search=john+doe');
  });

  it('calls backend list endpoint with unassigned availability filter', async () => {
    const fetchMock = mockFetchResponse({ ok: true, body: createGuestPage() });

    await listGuests({ page: 0, size: 20, status: 'active', availability: 'unassigned' });

    const [url] = getFirstRequest(fetchMock);
    expect(url).toBe('http://localhost:8080/api/guests?page=0&size=20&status=active&availability=unassigned');
  });

  it('calls backend create endpoint with expected payload', async () => {
    setCsrfCookie();

    const fetchMock = mockFetchResponse({ ok: true, body: { id: 42 } });

    const payload = createGuestPayload();

    const result = await addGuest(payload);

    expect(fetchMock).toHaveBeenCalledTimes(1);
    const [url, options] = getFirstRequest(fetchMock);

    expect(url).toBe('http://localhost:8080/api/guests');
    expect(options).toMatchObject({
      method: 'POST',
      credentials: 'include',
      body: JSON.stringify(payload)
    });
    expect(options?.headers).toBeInstanceOf(Headers);

    const headers = options.headers as Headers;
    expect(headers.get('Content-Type')).toBe('application/json');
    expectCsrfHeader(options);
    expect(result).toEqual({ id: 42 });
  });

  it('throws when create endpoint response is not ok', async () => {
    mockFetchResponse({ ok: false });

    await expect(addGuest(createGuestPayload())).rejects.toThrow('Unable to create guest at the moment.');
  });

  it('calls backend detail endpoint by guest id', async () => {
    const guest = createGuestResponse({ id: 'abc-123' });
    const fetchMock = mockFetchResponse({ ok: true, body: guest });

    const result = await getGuestById('abc-123');

    expect(fetchMock).toHaveBeenCalledWith('http://localhost:8080/api/guests/abc-123', {
      method: 'GET',
      credentials: 'include'
    });
    expect(result).toEqual(guest);
  });

  it('throws not found error when detail endpoint returns 404', async () => {
    mockFetchResponse({ ok: false, status: 404 });

    await expect(getGuestById('missing')).rejects.toThrow(guestNotFoundMessage);
  });

  it('calls backend update endpoint with expected payload', async () => {
    setCsrfCookie();

    const fetchMock = mockFetchResponse({
      ok: true,
      body: createGuestResponse({ id: 'guest-1', version: 2 })
    });

    const payload = {
      ...createGuestPayload(),
      version: 1
    };

    await updateGuest('guest-1', payload);

    expect(fetchMock).toHaveBeenCalledTimes(1);
    const [url, options] = getFirstRequest(fetchMock);
    expect(url).toBe('http://localhost:8080/api/guests/guest-1');
    expect(options).toMatchObject({
      method: 'PUT',
      credentials: 'include',
      body: JSON.stringify(payload)
    });

    const headers = options.headers as Headers;
    expect(headers.get('Content-Type')).toBe('application/json');
    expectCsrfHeader(options);
  });

  it('throws conflict error when update endpoint returns 409', async () => {
    mockFetchResponse({ ok: false, status: 409 });

    await expect(updateGuest('guest-1', {
      ...createGuestPayload(),
      version: 3
    })).rejects.toThrow('This guest has been modified elsewhere. Please reload and try again.');
  });

  it('calls backend archive endpoint with csrf header', async () => {
    setCsrfCookie();

    const fetchMock = mockFetchResponse({
      ok: true,
      body: createGuestResponse({ id: 'guest-1', version: 2 })
    });

    await archiveGuest('guest-1');

    const [url, options] = getFirstRequest(fetchMock);
    expect(url).toBe('http://localhost:8080/api/guests/guest-1');
    expect(options).toMatchObject({
      method: 'DELETE',
      credentials: 'include'
    });

    expectCsrfHeader(options);
  });

  it('throws not found when archive endpoint returns 404', async () => {
    mockFetchResponse({ ok: false, status: 404 });

    await expect(archiveGuest('missing')).rejects.toThrow(guestNotFoundMessage);
  });

  it('calls backend restoration endpoint with csrf header', async () => {
    setCsrfCookie();

    const fetchMock = mockFetchResponse({
      ok: true,
      body: createGuestResponse({ id: 'guest-1', version: 3 })
    });

    await restoreGuest('guest-1');

    const [url, options] = getFirstRequest(fetchMock);
    expect(url).toBe('http://localhost:8080/api/guests/guest-1/restoration');
    expect(options).toMatchObject({
      method: 'POST',
      credentials: 'include'
    });

    expectCsrfHeader(options);
  });

  it('throws not found when restoration endpoint returns 404', async () => {
    mockFetchResponse({ ok: false, status: 404 });

    await expect(restoreGuest('missing')).rejects.toThrow(guestNotFoundMessage);
  });
});