const normalizeBaseUrl = (value: string) => value.replace(/\/+$/, '');

const localPublicPortByBackofficePort: Record<string, string> = {
  '5173': '5174'
};

export const inferLocalPublicAppOrigin = (origin: string): string | undefined => {
  let parsedOrigin: URL;

  try {
    parsedOrigin = new URL(origin);
  } catch {
    return undefined;
  }

  const { hostname, port, protocol } = parsedOrigin;

  if (hostname !== 'localhost' && hostname !== '127.0.0.1') {
    return undefined;
  }

  const publicPort = localPublicPortByBackofficePort[port];

  if (!publicPort) {
    return undefined;
  }

  return `${protocol}//${hostname}:${publicPort}`;
};

const getGuestAccessBaseUrl = (): string => {
  const envBaseUrl = import.meta.env.VITE_PUBLIC_APP_BASE_URL?.trim();

  if (envBaseUrl) {
    return normalizeBaseUrl(envBaseUrl);
  }

  if (typeof window !== 'undefined' && window.location.origin) {
    const inferredLocalPublicOrigin = inferLocalPublicAppOrigin(window.location.origin);

    if (inferredLocalPublicOrigin) {
      return normalizeBaseUrl(inferredLocalPublicOrigin);
    }

    return normalizeBaseUrl(window.location.origin);
  }

  throw new Error('Missing public app base URL configuration.');
};

export const buildGuestAccessUrl = (accessToken?: string | null): string | undefined => {
  const normalizedToken = accessToken?.trim();

  if (!normalizedToken) {
    return undefined;
  }

  return `${getGuestAccessBaseUrl()}/guest-access/${encodeURIComponent(normalizedToken)}`;
};


