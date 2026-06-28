import { afterEach, describe, expect, it, vi } from 'vitest';
import { addGuest, archiveGuest, getGuestById, listGuests, restoreGuest, updateGuest } from './guestApi';
import { createGuestPage, createGuestPayload, createGuestResponse } from '../testFixtures/guestFixtures';

describe('guestApi', () => {
  afterEach(() => {
    vi.restoreAllMocks();
    document.cookie = 'XSRF-TOKEN=; Max-Age=0; path=/';
  });

  it('calls backend list endpoint with default pagination', async () => {
    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: true,
      json: async () => createGuestPage({
        items: [],
        totalItems: 0,
        totalPages: 0
      })
    } as Response);

    await listGuests();

    expect(fetchMock).toHaveBeenCalledTimes(1);
    const [url, options] = fetchMock.mock.calls[0];

    expect(url).toBe('http://localhost:8080/api/guests?page=0&size=20&status=active');
    expect(options).toMatchObject({
      method: 'GET',
      credentials: 'include'
    });
  });

  it('throws when list endpoint response is not ok', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: false,
      json: async () => ({})
    } as Response);

    await expect(listGuests()).rejects.toThrow('Unable to retrieve guests at the moment.');
  });

  it('calls backend list endpoint with archived status', async () => {
    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: true,
      json: async () => createGuestPage()
    } as Response);

    await listGuests({ page: 1, size: 50, status: 'archived' });

    const [url] = fetchMock.mock.calls[0];
    expect(url).toBe('http://localhost:8080/api/guests?page=1&size=50&status=archived');
  });

  it('calls backend create endpoint with expected payload', async () => {
    document.cookie = 'XSRF-TOKEN=test-csrf-token';

    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: true,
      json: async () => ({ id: 42 })
    } as Response);

    const payload = createGuestPayload();

    const result = await addGuest(payload);

    expect(fetchMock).toHaveBeenCalledTimes(1);
    const [url, options] = fetchMock.mock.calls[0];

    expect(url).toBe('http://localhost:8080/api/guests');
    expect(options).toMatchObject({
      method: 'POST',
      credentials: 'include',
      body: JSON.stringify(payload)
    });
    expect(options?.headers).toBeInstanceOf(Headers);

    const headers = options?.headers as Headers;
    expect(headers.get('Content-Type')).toBe('application/json');
    expect(headers.get('X-XSRF-TOKEN')).toBe('test-csrf-token');
    expect(result).toEqual({ id: 42 });
  });

  it('throws when create endpoint response is not ok', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: false,
      json: async () => ({})
    } as Response);

    await expect(addGuest(createGuestPayload())).rejects.toThrow('Unable to create guest at the moment.');
  });

  it('calls backend detail endpoint by guest id', async () => {
    const guest = createGuestResponse({ id: 'abc-123' });
    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: true,
      status: 200,
      json: async () => guest
    } as Response);

    const result = await getGuestById('abc-123');

    expect(fetchMock).toHaveBeenCalledWith('http://localhost:8080/api/guests/abc-123', {
      method: 'GET',
      credentials: 'include'
    });
    expect(result).toEqual(guest);
  });

  it('throws not found error when detail endpoint returns 404', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: false,
      status: 404,
      json: async () => ({})
    } as Response);

    await expect(getGuestById('missing')).rejects.toThrow('Guest not found.');
  });

  it('calls backend update endpoint with expected payload', async () => {
    document.cookie = 'XSRF-TOKEN=test-csrf-token';

    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: true,
      status: 200,
      json: async () => createGuestResponse({ id: 'guest-1', version: 2 })
    } as Response);

    const payload = {
      ...createGuestPayload(),
      version: 1
    };

    await updateGuest('guest-1', payload);

    expect(fetchMock).toHaveBeenCalledTimes(1);
    const [url, options] = fetchMock.mock.calls[0];
    expect(url).toBe('http://localhost:8080/api/guests/guest-1');
    expect(options).toMatchObject({
      method: 'PUT',
      credentials: 'include',
      body: JSON.stringify(payload)
    });

    const headers = options?.headers as Headers;
    expect(headers.get('Content-Type')).toBe('application/json');
    expect(headers.get('X-XSRF-TOKEN')).toBe('test-csrf-token');
  });

  it('throws conflict error when update endpoint returns 409', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: false,
      status: 409,
      json: async () => ({})
    } as Response);

    await expect(updateGuest('guest-1', {
      ...createGuestPayload(),
      version: 3
    })).rejects.toThrow('This guest has been modified elsewhere. Please reload and try again.');
  });

  it('calls backend archive endpoint with csrf header', async () => {
    document.cookie = 'XSRF-TOKEN=test-csrf-token';

    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: true,
      status: 200,
      json: async () => createGuestResponse({ id: 'guest-1', version: 2 })
    } as Response);

    await archiveGuest('guest-1');

    const [url, options] = fetchMock.mock.calls[0];
    expect(url).toBe('http://localhost:8080/api/guests/guest-1');
    expect(options).toMatchObject({
      method: 'DELETE',
      credentials: 'include'
    });

    const headers = options?.headers as Headers;
    expect(headers.get('X-XSRF-TOKEN')).toBe('test-csrf-token');
  });

  it('throws not found when archive endpoint returns 404', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: false,
      status: 404,
      json: async () => ({})
    } as Response);

    await expect(archiveGuest('missing')).rejects.toThrow('Guest not found.');
  });

  it('calls backend restoration endpoint with csrf header', async () => {
    document.cookie = 'XSRF-TOKEN=test-csrf-token';

    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: true,
      status: 200,
      json: async () => createGuestResponse({ id: 'guest-1', version: 3 })
    } as Response);

    await restoreGuest('guest-1');

    const [url, options] = fetchMock.mock.calls[0];
    expect(url).toBe('http://localhost:8080/api/guests/guest-1/restoration');
    expect(options).toMatchObject({
      method: 'POST',
      credentials: 'include'
    });

    const headers = options?.headers as Headers;
    expect(headers.get('X-XSRF-TOKEN')).toBe('test-csrf-token');
  });

  it('throws not found when restoration endpoint returns 404', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: false,
      status: 404,
      json: async () => ({})
    } as Response);

    await expect(restoreGuest('missing')).rejects.toThrow('Guest not found.');
  });
});