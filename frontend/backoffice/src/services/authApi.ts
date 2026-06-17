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

export const getAuthStatus = async (): Promise<AuthStatus> => {
  const response = await fetch(`${getApiBaseUrl()}/auth/me`, {
    credentials: 'include',
  });

  if (!response.ok) {
    throw new Error('Unable to retrieve authentication status.');
  }

  return response.json();
};

export const getGoogleLoginUrl = (): string => `${getApiBaseUrl()}/oauth2/authorization/google`;
