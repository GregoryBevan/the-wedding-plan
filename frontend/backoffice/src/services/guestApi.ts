export interface CreateGuestPayload {
  firstName: string;
  lastName: string;
  email: string;
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
  const apiBaseUrl = import.meta.env.VITE_API_BASE_URL + "/api";

  if (!apiBaseUrl) {
    throw new Error('Missing VITE_API_BASE_URL environment variable.');
  }

  return apiBaseUrl;
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