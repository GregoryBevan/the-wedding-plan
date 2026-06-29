const getApiBaseUrl = (): string => {
  const apiBaseUrl = import.meta.env.VITE_API_BASE_URL?.trim();

  if (apiBaseUrl) {
    return apiBaseUrl;
  }

  if (typeof window !== 'undefined' && window.location.origin) {
    return window.location.origin;
  }

  throw new Error('Missing API base URL configuration.');
};

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

export interface AuthStatus {
  isAuthenticated: boolean;
  email: string | null;
  isAuthorized: boolean;
}

interface RawAuthStatus {
  isAuthenticated?: boolean;
  authenticated?: boolean;
  email?: string | null;
  isAuthorized?: boolean;
  authorized?: boolean;
}

export const getAuthStatus = async (): Promise<AuthStatus> => {
  const response = await fetch(`${getApiBaseUrl()}/auth/me`, {
    credentials: 'include',
  });

  if (!response.ok) {
    throw new Error('Unable to retrieve authentication status.');
  }

  const payload = await response.json() as RawAuthStatus;

  return {
    isAuthenticated: payload.isAuthenticated ?? payload.authenticated ?? false,
    email: payload.email ?? null,
    isAuthorized: payload.isAuthorized ?? payload.authorized ?? false,
  };
};

export const getGoogleLoginUrl = (): string => `${getApiBaseUrl()}/oauth2/authorization/google`;

export const logout = async (): Promise<void> => {
  const csrfToken = readCookie('XSRF-TOKEN');
  const headers = new Headers();

  if (csrfToken) {
    headers.set('X-XSRF-TOKEN', csrfToken);
  }

  const response = await fetch(`${getApiBaseUrl()}/auth/logout`, {
    method: 'POST',
    credentials: 'include',
    headers
  });

  if (!response.ok) {
    throw new Error('Unable to log out.');
  }
};
