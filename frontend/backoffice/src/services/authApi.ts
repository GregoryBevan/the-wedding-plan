import { getApiBaseUrl, readCookie } from './http';

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
