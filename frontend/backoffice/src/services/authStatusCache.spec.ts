import { afterEach, describe, expect, it, vi } from 'vitest';
import { clearSessionAuthStatus, getSessionAuthStatus } from './authStatusCache';

const authApiMock = vi.hoisted(() => ({
  getAuthStatus: vi.fn()
}));

vi.mock('./authApi', () => ({
  getAuthStatus: authApiMock.getAuthStatus
}));

describe('authStatusCache', () => {
  afterEach(() => {
    clearSessionAuthStatus();
    vi.clearAllMocks();
  });

  it('reuses cached auth status across calls', async () => {
    authApiMock.getAuthStatus.mockResolvedValue({
      isAuthenticated: true,
      email: 'allowed@example.com',
      isAuthorized: true
    });

    const first = await getSessionAuthStatus();
    const second = await getSessionAuthStatus();

    expect(authApiMock.getAuthStatus).toHaveBeenCalledTimes(1);
    expect(first).toEqual(second);
  });

  it('reuses in-flight request for concurrent calls', async () => {
    let resolveRequest: ((value: unknown) => void) | null = null;
    authApiMock.getAuthStatus.mockImplementation(() => new Promise((resolve) => {
      resolveRequest = resolve;
    }));

    const pendingFirst = getSessionAuthStatus();
    const pendingSecond = getSessionAuthStatus();

    resolveRequest!({
      isAuthenticated: true,
      email: 'allowed@example.com',
      isAuthorized: true
    });

    const [first, second] = await Promise.all([pendingFirst, pendingSecond]);

    expect(authApiMock.getAuthStatus).toHaveBeenCalledTimes(1);
    expect(first).toEqual(second);
  });

  it('clears cache when requested', async () => {
    authApiMock.getAuthStatus
      .mockResolvedValueOnce({
        isAuthenticated: true,
        email: 'first@example.com',
        isAuthorized: true
      })
      .mockResolvedValueOnce({
        isAuthenticated: true,
        email: 'second@example.com',
        isAuthorized: true
      });

    const first = await getSessionAuthStatus();
    clearSessionAuthStatus();
    const second = await getSessionAuthStatus();

    expect(authApiMock.getAuthStatus).toHaveBeenCalledTimes(2);
    expect(first.email).toBe('first@example.com');
    expect(second.email).toBe('second@example.com');
  });
});

