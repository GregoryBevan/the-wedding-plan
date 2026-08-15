import type { AuthStatus } from '../services/authApi';

export const adminAuthStatus: AuthStatus = {
  isAuthenticated: true,
  email: 'allowed@example.com',
  isAuthorized: true,
  canWrite: true
};

export const readOnlyAuthStatus: AuthStatus = {
  isAuthenticated: true,
  email: 'viewer@example.com',
  isAuthorized: true,
  canWrite: false
};

export const unauthorizedAuthStatus: AuthStatus = {
  isAuthenticated: true,
  email: 'someone@example.com',
  isAuthorized: false,
  canWrite: false
};

export const unauthenticatedAuthStatus: AuthStatus = {
  isAuthenticated: false,
  email: null,
  isAuthorized: false,
  canWrite: false
};

export const createAuthStatus = (overrides: Partial<AuthStatus> = {}): AuthStatus => ({
  ...adminAuthStatus,
  ...overrides
});

