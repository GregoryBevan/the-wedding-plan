import { afterEach, describe, expect, it, vi } from 'vitest';
import { listInvitations } from './invitationApi';

describe('invitationApi', () => {
  afterEach(() => {
    vi.restoreAllMocks();
    document.cookie = 'XSRF-TOKEN=; Max-Age=0; path=/';
  });

  it('calls backend invitations list endpoint with pagination and csrf header', async () => {
    document.cookie = 'XSRF-TOKEN=test-csrf-token';

    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: true,
      json: async () => ({
        items: [],
        page: 0,
        size: 20,
        totalItems: 0,
        totalPages: 0
      })
    } as Response);

    await listInvitations({ page: 1, size: 10 });

    const [url, options] = fetchMock.mock.calls[0] as [string, RequestInit];

    expect(url).toBe('http://localhost:8080/api/invitations?page=1&size=10');
    expect(options).toMatchObject({
      method: 'GET',
      credentials: 'include'
    });

    const headers = options.headers as Headers;
    expect(headers.get('X-XSRF-TOKEN')).toBe('test-csrf-token');
  });

  it('throws when list endpoint response is not ok', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: false,
      json: async () => ({})
    } as Response);

    await expect(listInvitations()).rejects.toThrow('Unable to retrieve invitations at the moment.');
  });
});

