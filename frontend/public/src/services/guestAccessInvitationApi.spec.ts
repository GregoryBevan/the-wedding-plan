import { afterEach, describe, expect, it, vi } from 'vitest';
import { GuestAccessInvitationApiError, resolveInvitationByToken } from './guestAccessInvitationApi';
import { getFirstRequest, mockFetchResponse } from '../testFixtures/httpTestHelpers';

describe('guestAccessInvitationApi', () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('resolves an invitation from the guest-access API', async () => {
    const invitation = { label: 'Bridesmaid', description: 'Welcome', guests: [], guestCount: 0 };
    const fetchMock = mockFetchResponse({ ok: true, body: invitation });

    const result = await resolveInvitationByToken('a b/c');

    const [url, options] = getFirstRequest(fetchMock);
    expect(url).toBe('http://localhost:8080/api/guest-access/invitations/a%20b%2Fc');
    expect(options).toMatchObject({ method: 'GET' });
    expect(result).toEqual(invitation);
  });

  it('throws a typed error when the response is not ok', async () => {
    mockFetchResponse({ ok: false, status: 404 });

    await expect(resolveInvitationByToken('token')).rejects.toBeInstanceOf(GuestAccessInvitationApiError);
    await expect(resolveInvitationByToken('token')).rejects.toThrow('Unable to resolve invitation.');
  });
});

