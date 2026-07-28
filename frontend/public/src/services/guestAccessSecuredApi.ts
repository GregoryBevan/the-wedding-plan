import { getApiBaseUrl, readCookie } from './http';

const CSRF_COOKIE = 'XSRF-TOKEN';
const CSRF_HEADER = 'X-XSRF-TOKEN';
const MUTATING_METHODS = new Set(['POST', 'PUT', 'PATCH', 'DELETE']);

const guestAccessSecuredBaseUrl = `${getApiBaseUrl({ includeApiPath: true })}/guest-access/secured`;

export class GuestAccessSecuredApiError extends Error {
  constructor(
    message: string,
    readonly status: number,
  ) {
    super(message);
    this.name = 'GuestAccessSecuredApiError';
  }
}

export type GuestLanguage = 'FR' | 'EN';

/**
 * Identity of the currently verified guest, resolved from the `guest_session` cookie.
 */
export interface GuestSessionResponse {
  guestId: string;
  invitationId: string;
  firstName: string;
  lastName: string;
  language: GuestLanguage;
}

/**
 * Performs a request against the secured guest API (`/api/guest-access/secured/**`).
 *
 * It always sends the credentials so the browser attaches the `guest_session`
 * (JWT, HttpOnly) cookie, and adds the CSRF header from the `XSRF-TOKEN` cookie
 * for mutating requests.
 */
export const securedGuestFetch = async (path: string, init: RequestInit = {}): Promise<Response> => {
  const method = (init.method ?? 'GET').toUpperCase();
  const headers = new Headers(init.headers);

  if (MUTATING_METHODS.has(method)) {
    const csrfToken = readCookie(CSRF_COOKIE);

    if (csrfToken) {
      headers.set(CSRF_HEADER, csrfToken);
    }
  }

  return fetch(`${guestAccessSecuredBaseUrl}${path}`, {
    ...init,
    method,
    credentials: 'include',
    headers,
  });
};

/**
 * Resolves the currently verified guest session (`GET /api/guest-access/secured/me`).
 *
 * Used both to confirm that a magic link was successfully consumed and to gate the
 * RSVP/choices form. Returns `null` when there is no usable verified session, i.e.
 * either no/expired session (`401`) or a session whose guest is no longer part of the
 * invitation (`403`), so the caller can treat "not verified" as an expected,
 * recoverable state rather than a transient error.
 */
export const fetchGuestSession = async (): Promise<GuestSessionResponse | null> => {
  const response = await securedGuestFetch('/me');

  if (response.status === 401 || response.status === 403) {
    return null;
  }

  if (!response.ok) {
    throw new GuestAccessSecuredApiError('Unable to load the guest session.', response.status);
  }

  return response.json() as Promise<GuestSessionResponse>;
};

/**
 * Placeholder secured mutation ready for the upcoming RSVP feature.
 * The backend endpoint currently returns 204 No Content.
 */
export const submitRsvp = async (): Promise<void> => {
  const response = await securedGuestFetch('/rsvp', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
  });

  if (!response.ok) {
    throw new GuestAccessSecuredApiError('Unable to submit RSVP at the moment.', response.status);
  }
};

