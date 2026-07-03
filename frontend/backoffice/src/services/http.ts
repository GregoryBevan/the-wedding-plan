interface ApiBaseUrlOptions {
  includeApiPath?: boolean;
}

const normalizeBaseUrl = (value: string) => value.replace(/\/+$/, '');

export const getApiBaseUrl = ({ includeApiPath = false }: ApiBaseUrlOptions = {}): string => {
  const envBaseUrl = import.meta.env.VITE_API_BASE_URL?.trim();

  if (envBaseUrl) {
    const normalizedBaseUrl = normalizeBaseUrl(envBaseUrl);

    return includeApiPath ? `${normalizedBaseUrl}/api` : normalizedBaseUrl;
  }

  if (typeof window !== 'undefined' && window.location.origin) {
    const normalizedOrigin = normalizeBaseUrl(window.location.origin);

    return includeApiPath ? `${normalizedOrigin}/api` : normalizedOrigin;
  }

  throw new Error('Missing API base URL configuration.');
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

