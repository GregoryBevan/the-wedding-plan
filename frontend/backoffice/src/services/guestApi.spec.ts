import { afterEach, describe, expect, it, vi } from 'vitest';
import { addGuest } from './guestApi';

describe('addGuest', () => {
  afterEach(() => {
    vi.restoreAllMocks();
    document.cookie = 'XSRF-TOKEN=; Max-Age=0; path=/';
  });

  it('calls backend endpoint with expected payload', async () => {
    document.cookie = 'XSRF-TOKEN=test-csrf-token';

    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: true,
      json: async () => ({ id: 42 })
    } as Response);

    const payload = {
      firstName: 'John',
      lastName: 'Doe',
      email: 'john.doe@email.com'
    };

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

  it('throws when backend response is not ok', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: false,
      json: async () => ({})
    } as Response);

    await expect(addGuest({
      firstName: 'John',
      lastName: 'Doe',
      email: 'john.doe@email.com'
    })).rejects.toThrow('Unable to create guest at the moment.');
  });
});