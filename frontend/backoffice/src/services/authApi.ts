const getApiBaseUrl = (): string => {
  const apiBaseUrl = import.meta.env.VITE_API_BASE_URL;

  if (!apiBaseUrl) {
    throw new Error('Missing VITE_API_BASE_URL environment variable.');
  }

  return apiBaseUrl;
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
