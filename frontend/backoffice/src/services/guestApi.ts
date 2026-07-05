import { getApiBaseUrl, readCookie } from './http';

export interface AddGuestPayload {
  firstName: string;
  lastName: string;
  email: string;
}

export interface EditGuestPayload extends AddGuestPayload {
  version: number;
}

export interface GuestResponse {
  id: string;
  version: number;
  creationDate: string;
  updateDate: string;
  firstName: string;
  lastName: string;
  email: string;
}

export interface GuestPageResponse {
  items: GuestResponse[];
  page: number;
  size: number;
  totalItems: number;
  totalPages: number;
}

export type GuestStatus = 'active' | 'archived' | 'all';
export type GuestAvailability = 'all' | 'unassigned';

const guestApiBaseUrl = getApiBaseUrl({ includeApiPath: true });

export const listGuests = async (
  {
    page = 0,
    size = 20,
    status = 'active',
    availability = 'all',
    search
  }: { page?: number; size?: number; status?: GuestStatus; availability?: GuestAvailability; search?: string } = {}
): Promise<GuestPageResponse> => {
  const queryParams = new URLSearchParams({
    page: String(page),
    size: String(size),
    status,
    availability
  });

  const normalizedSearch = search?.trim();

  if (normalizedSearch) {
    queryParams.set('search', normalizedSearch);
  }

  const response = await fetch(`${guestApiBaseUrl}/guests?${queryParams}`, {
    method: 'GET',
    credentials: 'include'
  });

  if (!response.ok) {
    throw new Error('Unable to retrieve guests at the moment.');
  }

  return response.json() as Promise<GuestPageResponse>;
};

export const addGuest = async (payload: AddGuestPayload): Promise<GuestResponse> => {
  const csrfToken = readCookie('XSRF-TOKEN');

  const headers = new Headers({
    'Content-Type': 'application/json'
  });

  if (csrfToken) {
    headers.set('X-XSRF-TOKEN', csrfToken);
  }

  const response = await fetch(`${guestApiBaseUrl}/guests`, {
    method: 'POST',
    credentials: 'include',
    headers,
    body: JSON.stringify(payload)
  });

  if (!response.ok) {
    throw new Error('Unable to create guest at the moment.');
  }

  return await response.json() as Promise<GuestResponse>;
};

export const getGuestById = async (id: string): Promise<GuestResponse> => {
  const response = await fetch(`${guestApiBaseUrl}/guests/${id}`, {
    method: 'GET',
    credentials: 'include'
  });

  if (response.status === 404) {
    throw new Error('Guest not found.');
  }

  if (!response.ok) {
    throw new Error('Unable to retrieve guest at the moment.');
  }

  return await response.json() as Promise<GuestResponse>;
};

export const updateGuest = async (id: string, payload: EditGuestPayload): Promise<GuestResponse> => {
  const csrfToken = readCookie('XSRF-TOKEN');

  const headers = new Headers({
    'Content-Type': 'application/json'
  });

  if (csrfToken) {
    headers.set('X-XSRF-TOKEN', csrfToken);
  }

  const response = await fetch(`${guestApiBaseUrl}/guests/${id}`, {
    method: 'PUT',
    credentials: 'include',
    headers,
    body: JSON.stringify(payload)
  });

  if (response.status === 404) {
    throw new Error('Guest not found.');
  }

  if (response.status === 409) {
    throw new Error('This guest has been modified elsewhere. Please reload and try again.');
  }

  if (!response.ok) {
    throw new Error('Unable to update guest at the moment.');
  }

  return await response.json() as Promise<GuestResponse>;
};

export const archiveGuest = async (id: string): Promise<GuestResponse> => {
  const csrfToken = readCookie('XSRF-TOKEN');

  const headers = new Headers();
  if (csrfToken) {
    headers.set('X-XSRF-TOKEN', csrfToken);
  }

  const response = await fetch(`${guestApiBaseUrl}/guests/${id}`, {
    method: 'DELETE',
    credentials: 'include',
    headers
  });

  if (response.status === 404) {
    throw new Error('Guest not found.');
  }

  if (!response.ok) {
    throw new Error('Unable to archive guest at the moment.');
  }

  return await response.json() as Promise<GuestResponse>;
};

export const restoreGuest = async (id: string): Promise<GuestResponse> => {
  const csrfToken = readCookie('XSRF-TOKEN');

  const headers = new Headers();
  if (csrfToken) {
    headers.set('X-XSRF-TOKEN', csrfToken);
  }

  const response = await fetch(`${guestApiBaseUrl}/guests/${id}/restoration`, {
    method: 'POST',
    credentials: 'include',
    headers
  });

  if (response.status === 404) {
    throw new Error('Guest not found.');
  }

  if (!response.ok) {
    throw new Error('Unable to restore guest at the moment.');
  }

  return await response.json() as Promise<GuestResponse>;
};
