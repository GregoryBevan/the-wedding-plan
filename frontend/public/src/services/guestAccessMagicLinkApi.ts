import { getApiBaseUrl, readCookie } from './http';

const CSRF_COOKIE = 'XSRF-TOKEN';
const CSRF_HEADER = 'X-XSRF-TOKEN';

const guestAccessApiBaseUrl = getApiBaseUrl({ includeApiPath: true });

export type MagicLinkRequestResult =
  | { status: 'accepted' }
  | { status: 'rateLimited'; retryAfterSeconds: number | null };

export class GuestAccessMagicLinkApiError extends Error {
  constructor(
    message: string,
    readonly status: number,
  ) {
    super(message);
    this.name = 'GuestAccessMagicLinkApiError';
  }
}

/**
 * Requests a magic link for a given guest of an invitation.
 *
 * Maps to `POST /api/guest-access/invitations/{token}/guests/{guestId}/magic-link-requests`.
 * The backend answers `202 Accepted` (the email is always sent asynchronously and
 * the response is intentionally opaque) or `429 Too Many Requests` when rate limited.
 * Credentials are sent so the CSRF cookie is attached, and the CSRF header is added
 * from the `XSRF-TOKEN` cookie for this mutating request.
 */
export const requestMagicLink = async (
  invitationToken: string,
  guestId: string,
): Promise<MagicLinkRequestResult> => {
  const headers = new Headers({ Accept: 'application/json' });
  const csrfToken = readCookie(CSRF_COOKIE);

  if (csrfToken) {
    headers.set(CSRF_HEADER, csrfToken);
  }

  const url =
    `${guestAccessApiBaseUrl}/guest-access/invitations/${encodeURIComponent(invitationToken)}` +
    `/guests/${encodeURIComponent(guestId)}/magic-link-requests`;

  const response = await fetch(url, {
    method: 'POST',
    credentials: 'include',
    headers,
  });

  if (response.status === 429) {
    const retryAfterHeader = response.headers.get('Retry-After');
    const retryAfterSeconds = retryAfterHeader === null ? Number.NaN : Number(retryAfterHeader);

    return {
      status: 'rateLimited',
      retryAfterSeconds: Number.isFinite(retryAfterSeconds) ? retryAfterSeconds : null,
    };
  }

  if (!response.ok) {
    throw new GuestAccessMagicLinkApiError('Unable to request the magic link.', response.status);
  }

  return { status: 'accepted' };
};


