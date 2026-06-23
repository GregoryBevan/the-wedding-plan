import { getAuthStatus, type AuthStatus } from './authApi';

let cachedAuthStatus: AuthStatus | null = null;
let inFlightAuthStatusRequest: Promise<AuthStatus> | null = null;

export const getSessionAuthStatus = async (): Promise<AuthStatus> => {
  if (cachedAuthStatus) {
    return cachedAuthStatus;
  }

  if (inFlightAuthStatusRequest) {
    return inFlightAuthStatusRequest;
  }

  inFlightAuthStatusRequest = getAuthStatus()
    .then((status) => {
      cachedAuthStatus = status;
      return status;
    })
    .finally(() => {
      inFlightAuthStatusRequest = null;
    });

  return inFlightAuthStatusRequest;
};

export const clearSessionAuthStatus = () => {
  cachedAuthStatus = null;
  inFlightAuthStatusRequest = null;
};

