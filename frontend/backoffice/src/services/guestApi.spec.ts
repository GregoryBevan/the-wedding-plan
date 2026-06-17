import { afterEach, describe, expect, it, vi } from 'vitest';
import { addGuest } from './guestApi';

describe('addGuest', () => {
  afterEach(() => {
    vi.restoreAllMocks();
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

    expect(fetchMock).toHaveBeenCalledWith('http://localhost:8080/guests', {
      method: 'POST',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json',
        'X-XSRF-TOKEN': 'test-csrf-token'
      },
      body: JSON.stringify(payload)
    });
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