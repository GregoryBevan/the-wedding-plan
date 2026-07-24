interface ApiBaseUrlOptions {
  includeApiPath?: boolean;
}

const normalizeBaseUrl = (value: string) => value.replace(/\/+$/, '');

const resolveOrigin = (): string => {
  const envBaseUrl = import.meta.env.VITE_API_BASE_URL?.trim();

  if (envBaseUrl) {
    return normalizeBaseUrl(envBaseUrl);
  }

  if (typeof window !== 'undefined' && window.location.origin) {
    if (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1') {
      return 'http://localhost:8080';
    }

    return normalizeBaseUrl(window.location.origin);
  }

  throw new Error('Missing API base URL configuration.');
};

export const getApiBaseUrl = ({ includeApiPath = false }: ApiBaseUrlOptions = {}): string => {
  const origin = resolveOrigin();

  return includeApiPath ? `${origin}/api` : origin;
};

export const readCookie = (name: string): string | undefined => {
  if (typeof document === 'undefined') {
    return undefined;
  }

  const token = document.cookie
    .split(';')
    .map((cookie) => cookie.trim())
    .find((cookie) => cookie.startsWith(`${name}=`));

  return token ? decodeURIComponent(token.substring(name.length + 1)) : undefined;
};

