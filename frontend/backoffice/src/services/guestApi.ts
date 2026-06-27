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

const readCookie = (name: string): string | undefined => {
  if (typeof document === 'undefined') {
    return undefined;
  }

  const token = document.cookie
    .split(';')
    .map((cookie) => cookie.trim())
    .find((cookie) => cookie.startsWith(`${name}=`));

  return token ? decodeURIComponent(token.substring(name.length + 1)) : undefined;
};

const getApiBaseUrl = (): string => {
  const envBaseUrl = import.meta.env.VITE_API_BASE_URL;

  if (!envBaseUrl) {
    throw new Error('Missing VITE_API_BASE_URL environment variable.');
  }

  return envBaseUrl + "/api";
};

export const listGuests = async ({ page = 0, size = 20 }: { page?: number; size?: number } = {}): Promise<GuestPageResponse> => {
  const apiBaseUrl = getApiBaseUrl();
  const queryParams = new URLSearchParams({
    page: String(page),
    size: String(size)
  });

  const response = await fetch(`${apiBaseUrl}/guests?${queryParams}`, {
    method: 'GET',
    credentials: 'include'
  });

  if (!response.ok) {
    throw new Error('Unable to retrieve guests at the moment.');
  }

  return response.json() as Promise<GuestPageResponse>;
};

export const addGuest = async (payload: AddGuestPayload): Promise<GuestResponse> => {
  const apiBaseUrl = getApiBaseUrl();
  const csrfToken = readCookie('XSRF-TOKEN');

  const headers = new Headers({
    'Content-Type': 'application/json'
  });

  if (csrfToken) {
    headers.set('X-XSRF-TOKEN', csrfToken);
  }

  const response = await fetch(`${apiBaseUrl}/guests`, {
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
  const apiBaseUrl = getApiBaseUrl();
  const response = await fetch(`${apiBaseUrl}/guests/${id}`, {
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
  const apiBaseUrl = getApiBaseUrl();
  const csrfToken = readCookie('XSRF-TOKEN');

  const headers = new Headers({
    'Content-Type': 'application/json'
  });

  if (csrfToken) {
    headers.set('X-XSRF-TOKEN', csrfToken);
  }

  const response = await fetch(`${apiBaseUrl}/guests/${id}`, {
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