import { afterEach, describe, expect, it } from 'vitest';
import { buildGuestAccessUrl, inferLocalPublicAppOrigin } from './guestAccessUrl';

describe('guest access url helper', () => {
  const originalPublicAppBaseUrl = import.meta.env.VITE_PUBLIC_APP_BASE_URL;

  afterEach(() => {
    import.meta.env.VITE_PUBLIC_APP_BASE_URL = originalPublicAppBaseUrl;
  });

  it('returns undefined when token is missing', () => {
    expect(buildGuestAccessUrl(undefined)).toBeUndefined();
    expect(buildGuestAccessUrl('')).toBeUndefined();
    expect(buildGuestAccessUrl('   ')).toBeUndefined();
  });

  it('builds guest access url from configured public app base url', () => {
    import.meta.env.VITE_PUBLIC_APP_BASE_URL = 'http://localhost:5174///';

    expect(buildGuestAccessUrl('token-1')).toBe('http://localhost:5174/guest-access/token-1');
  });

  it('infers local public app origin from known backoffice dev origins', () => {
    expect(inferLocalPublicAppOrigin('http://localhost:5173')).toBe('http://localhost:5174');
    expect(inferLocalPublicAppOrigin('http://127.0.0.1:5173')).toBe('http://127.0.0.1:5174');
  });

  it('returns undefined for non-mapped or non-local origins', () => {
    expect(inferLocalPublicAppOrigin('http://localhost:9999')).toBeUndefined();
    expect(inferLocalPublicAppOrigin('https://backoffice.example.com')).toBeUndefined();
    expect(inferLocalPublicAppOrigin('invalid-origin')).toBeUndefined();
  });

  it('falls back to window origin when public app base url is not configured', () => {
    import.meta.env.VITE_PUBLIC_APP_BASE_URL = ' ';

    expect(buildGuestAccessUrl('token-1')).toBe(`${window.location.origin}/guest-access/token-1`);
  });

  it('encodes token value in generated url', () => {
    import.meta.env.VITE_PUBLIC_APP_BASE_URL = 'http://localhost:5174';

    expect(buildGuestAccessUrl('token with slash / and spaces')).toBe(
      'http://localhost:5174/guest-access/token%20with%20slash%20%2F%20and%20spaces'
    );
  });
});

