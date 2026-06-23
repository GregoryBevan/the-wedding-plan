import { getAuthStatus, type AuthStatus } from './authApi';

const AUTH_CACHE_TTL_MS = 10 * 60 * 1000;

let cachedAuthStatus: AuthStatus | null = null;
let cacheTimestamp: number | null = null;
let cacheGeneration = 0;
let inFlightAuthStatusRequest: Promise<AuthStatus> | null = null;

export const getSessionAuthStatus = async (): Promise<AuthStatus> => {
  const now = Date.now();

  if (cachedAuthStatus) {
    if (cacheTimestamp && now - cacheTimestamp < AUTH_CACHE_TTL_MS) {
      return cachedAuthStatus;
    }

    cachedAuthStatus = null;
    cacheTimestamp = null;
  }

  if (inFlightAuthStatusRequest) {
    return inFlightAuthStatusRequest;
  }

  const requestGeneration = cacheGeneration;
  inFlightAuthStatusRequest = getAuthStatus()
    .then((status) => {
      if (requestGeneration !== cacheGeneration) {
        return status;
      }

      cacheTimestamp = Date.now();
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
  cacheTimestamp = null;
  cacheGeneration++;
  inFlightAuthStatusRequest = null;
};

