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

export type Meal = 'MEAT' | 'FISH' | 'VEGGIE';

/**
 * A song a guest can pick for the wedding playlist, matching the Deezer proxy and
 * RSVP song shapes. `preview` is an optional 30s audio URL.
 */
export interface Song {
  deezerId: number;
  title: string;
  artist: string;
  link: string;
  preview?: string | null;
}

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
 *
 * `meal` and `song` are only present for attending guests (both are reset when the
 * guest declines); `song` stays optional even when attending.
 */
export interface GuestRsvpResponse {
  id: string;
  version: number;
  creationDate: string;
  updateDate: string;
  attendance: RsvpAttendance;
  meal?: Meal | null;
  song?: Song | null;
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
 * Submits or updates the current guest's answer
 * (`POST /api/guest-access/secured/rsvp`) and returns the saved RSVP.
 *
 * A meal is mandatory to attend and irrelevant otherwise, so the overloads make it
 * required for `ATTENDING` and reject it for `DECLINED`. A song is optional even when
 * attending: pass a `Song` to set it, `null` to explicitly clear a previously saved
 * one, or omit it to leave the field out. The payload is derived from the attendance,
 * never from the meal/song truthiness.
 */
export function submitRsvp(attendance: 'ATTENDING', meal: Meal, song?: Song | null): Promise<GuestRsvpResponse>;
export function submitRsvp(attendance: 'DECLINED'): Promise<GuestRsvpResponse>;
export async function submitRsvp(
  attendance: RsvpAttendance,
  meal?: Meal,
  song?: Song | null,
): Promise<GuestRsvpResponse> {
  const payload =
    attendance === 'ATTENDING'
      ? song !== undefined
        ? { attendance, meal, song }
        : { attendance, meal }
      : { attendance };

  const response = await securedGuestFetch('/rsvp', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(payload),
  });

  if (!response.ok) {
    throw new GuestAccessSecuredApiError('Unable to submit RSVP at the moment.', response.status);
  }

  return response.json() as Promise<GuestRsvpResponse>;
}

/**
 * Searches the Deezer catalog through the proxy
 * (`GET /api/guest-access/secured/song-search?q=...`) and returns track suggestions.
 *
 * The backend returns an empty list for a blank query; a failure (e.g. the catalog is
 * unavailable) throws so the caller can surface a recoverable state without blocking,
 * since choosing a song is optional.
 */
export const searchSongs = async (query: string): Promise<Song[]> => {
  const response = await securedGuestFetch(`/song-search?q=${encodeURIComponent(query)}`);

  if (!response.ok) {
    throw new GuestAccessSecuredApiError('Unable to search songs at the moment.', response.status);
  }

  return response.json() as Promise<Song[]>;
};

