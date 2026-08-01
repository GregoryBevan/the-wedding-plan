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

export type RsvpAttendance = 'ATTENDING' | 'DECLINED';

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
 * A guest's stored RSVP, as returned by the secured RSVP endpoints.
 */
export interface GuestRsvpResponse {
  id: string;
  version: number;
  creationDate: string;
  updateDate: string;
  attendance: RsvpAttendance;
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
 * Loads the current guest's RSVP (`GET /api/guest-access/secured/rsvp`).
 *
 * Returns `null` when the guest has not responded yet (`204 No Content`), so the
 * form can render an empty state rather than treating "no answer yet" as an error.
 */
export const fetchRsvp = async (): Promise<GuestRsvpResponse | null> => {
  const response = await securedGuestFetch('/rsvp');

  if (response.status === 204) {
    return null;
  }

  if (!response.ok) {
    throw new GuestAccessSecuredApiError('Unable to load your RSVP at the moment.', response.status);
  }

  return response.json() as Promise<GuestRsvpResponse>;
};

/**
 * Submits or updates the current guest's attendance
 * (`POST /api/guest-access/secured/rsvp`) and returns the saved RSVP.
 */
export const submitRsvp = async (attendance: RsvpAttendance): Promise<GuestRsvpResponse> => {
  const response = await securedGuestFetch('/rsvp', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ attendance }),
  });

  if (!response.ok) {
    throw new GuestAccessSecuredApiError('Unable to submit RSVP at the moment.', response.status);
  }

  return response.json() as Promise<GuestRsvpResponse>;
};

