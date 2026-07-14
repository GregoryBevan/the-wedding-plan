import type { AuthStatus } from '../services/authApi';

export const authorizedAuthStatus: AuthStatus = {
  isAuthenticated: true,
  email: 'allowed@example.com',
  isAuthorized: true
};

export const unauthorizedAuthStatus: AuthStatus = {
  isAuthenticated: true,
  email: 'someone@example.com',
  isAuthorized: false
};

export const unauthenticatedAuthStatus: AuthStatus = {
  isAuthenticated: false,
  email: null,
  isAuthorized: false
};

export const createAuthStatus = (overrides: Partial<AuthStatus> = {}): AuthStatus => ({
  ...authorizedAuthStatus,
  ...overrides
});

