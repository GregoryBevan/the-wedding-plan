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

