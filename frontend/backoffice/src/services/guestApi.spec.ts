import { afterEach, describe, expect, it, vi } from 'vitest';
import { addGuest, listGuests } from './guestApi';
import { createGuestPage, createGuestPayload } from '../testFixtures/guestFixtures';

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

    expect(url).toBe('http://localhost:8080/api/guests?page=0&size=20');
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
});