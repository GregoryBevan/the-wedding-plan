import { afterEach, describe, expect, it, vi } from 'vitest';
import { getAuthStatus, getGoogleLoginUrl, logout } from './authApi';
import { clearCsrfCookie, getFirstRequest, mockFetchResponse, setCsrfCookie } from '../testFixtures/httpTestHelpers';

describe('authApi', () => {
  afterEach(() => {
    vi.restoreAllMocks();
    clearCsrfCookie();
  });

  it('retrieves auth status from backend', async () => {
    const fetchMock = mockFetchResponse({
      ok: true,
      body: {
        isAuthenticated: true,
        email: 'allowed@example.com',
        isAuthorized: true
      }
    });

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
    mockFetchResponse({
      ok: true,
      body: {
        authenticated: true,
        email: 'allowed@example.com',
        authorized: true
      }
    });

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
    setCsrfCookie();

    const fetchMock = mockFetchResponse({ ok: true });

    await logout();

    const [url, options] = getFirstRequest(fetchMock);

    expect(url).toBe('http://localhost:8080/auth/logout');
    expect(options.method).toBe('POST');
    expect(options.credentials).toBe('include');

    const headers = options.headers as Headers;
    expect(headers.get('X-XSRF-TOKEN')).toBe('test-csrf-token');
  });
});