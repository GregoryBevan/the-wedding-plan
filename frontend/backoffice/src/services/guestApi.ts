export interface CreateGuestPayload {
  firstName: string;
  lastName: string;
  email: string;
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

export const addGuest = async (payload: CreateGuestPayload) => {
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

  return response.json();
};