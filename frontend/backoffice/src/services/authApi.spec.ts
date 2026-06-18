import { afterEach, describe, expect, it, vi } from 'vitest';
import { getAuthStatus, getGoogleLoginUrl, logout } from './authApi';

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

  it('maps backend boolean fields without is-prefix', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: true,
      json: async () => ({
        authenticated: true,
        email: 'allowed@example.com',
        authorized: true
      })
    } as Response);

    const result = await getAuthStatus();

    expect(result).toEqual({
      isAuthenticated: true,
      email: 'allowed@example.com',
      isAuthorized: true
    });
  });

  it('returns backend google oauth2 login url', () => {
    expect(getGoogleLoginUrl()).toBe('http://localhost:8080/oauth2/authorization/google');
  });

  it('calls backend logout endpoint', async () => {
    Object.defineProperty(document, 'cookie', {
      configurable: true,
      value: 'XSRF-TOKEN=test-csrf-token'
    });

    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: true
    } as Response);

    await logout();

    const [url, options] = fetchMock.mock.calls[0] as [string, RequestInit];

    expect(url).toBe('http://localhost:8080/auth/logout');
    expect(options.method).toBe('POST');
    expect(options.credentials).toBe('include');

    const headers = options.headers as Headers;
    expect(headers.get('X-XSRF-TOKEN')).toBe('test-csrf-token');
  });
});