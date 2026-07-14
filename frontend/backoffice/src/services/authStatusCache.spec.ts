import { afterEach, describe, expect, it, vi } from 'vitest';
import { clearSessionAuthStatus, getSessionAuthStatus } from './authStatusCache';
import { createAuthStatus } from '../testFixtures/authFixtures';

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
    authApiMock.getAuthStatus.mockResolvedValue(createAuthStatus());

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

    resolveRequest!(createAuthStatus());

    const [first, second] = await Promise.all([pendingFirst, pendingSecond]);

    expect(authApiMock.getAuthStatus).toHaveBeenCalledTimes(1);
    expect(first).toEqual(second);
  });

  it('clears cache when requested', async () => {
    authApiMock.getAuthStatus
      .mockResolvedValueOnce(createAuthStatus({ email: 'first@example.com' }))
      .mockResolvedValueOnce(createAuthStatus({ email: 'second@example.com' }));

    const first = await getSessionAuthStatus();
    clearSessionAuthStatus();
    const second = await getSessionAuthStatus();

    expect(authApiMock.getAuthStatus).toHaveBeenCalledTimes(2);
    expect(first.email).toBe('first@example.com');
    expect(second.email).toBe('second@example.com');
  });

  it('refreshes cached data after TTL expires', async () => {
    const firstStatus = createAuthStatus({ email: 'first@example.com' });
    const secondStatus = createAuthStatus({ email: 'second@example.com' });

    authApiMock.getAuthStatus
      .mockResolvedValueOnce(firstStatus)
      .mockResolvedValueOnce(secondStatus);

    const first = await getSessionAuthStatus();
    expect(first.email).toBe('first@example.com');
    expect(authApiMock.getAuthStatus).toHaveBeenCalledTimes(1);

    vi.useFakeTimers();
    vi.setSystemTime(new Date(Date.now() + 11 * 60 * 1000));

    const second = await getSessionAuthStatus();
    expect(second.email).toBe('second@example.com');
    expect(authApiMock.getAuthStatus).toHaveBeenCalledTimes(2);

    vi.useRealTimers();
  });

  it('skips cache writes if cache was cleared while request was in-flight', async () => {
    let resolveRequest: ((value: unknown) => void) | null = null;
    authApiMock.getAuthStatus.mockImplementationOnce(() => new Promise((resolve) => {
      resolveRequest = resolve;
    }));
    authApiMock.getAuthStatus.mockResolvedValueOnce(createAuthStatus({ email: 'fresh@example.com' }));

    const pendingRequest = getSessionAuthStatus();

    clearSessionAuthStatus();

    resolveRequest!(createAuthStatus({ email: 'stale@example.com' }));

    await pendingRequest;

    const freshRequest = await getSessionAuthStatus();
    expect(freshRequest.email).toBe('fresh@example.com');
    expect(authApiMock.getAuthStatus).toHaveBeenCalledTimes(2);
  });
});

