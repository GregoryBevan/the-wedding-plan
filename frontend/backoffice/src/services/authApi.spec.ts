import { afterEach, describe, expect, it, vi } from 'vitest';
import { getAuthStatus, getGoogleLoginUrl } from './authApi';

describe('authApi', () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('retrieves auth status from backend', async () => {
    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: true,
      json: async () => ({
        isAuthenticated: true,
        email: 'allowed@example.com',
        isAuthorized: true
      })
    } as Response);

    const result = await getAuthStatus();

    expect(fetchMock).toHaveBeenCalledWith('http://localhost:8080/auth/me', {
      credentials: 'include'
    });
    expect(result).toEqual({
      isAuthenticated: true,
      email: 'allowed@example.com',
      isAuthorized: true
    });
  });

  it('returns backend google oauth2 login url', () => {
    expect(getGoogleLoginUrl()).toBe('http://localhost:8080/oauth2/authorization/google');
  });
});