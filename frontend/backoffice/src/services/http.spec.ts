import { afterEach, describe, expect, it } from 'vitest';
import { getApiBaseUrl, readCookie } from './http';

describe('http helpers', () => {
  const originalApiBaseUrl = import.meta.env.VITE_API_BASE_URL;

  afterEach(() => {
    import.meta.env.VITE_API_BASE_URL = originalApiBaseUrl;
    document.cookie = 'token=; Max-Age=0; path=/';
    document.cookie = 'XSRF-TOKEN=; Max-Age=0; path=/';
  });

  it('returns api base url from environment when configured', () => {
    import.meta.env.VITE_API_BASE_URL = 'https://api.example.com';

    expect(getApiBaseUrl()).toBe('https://api.example.com');
    expect(getApiBaseUrl({ includeApiPath: true })).toBe('https://api.example.com/api');
  });

  it('normalizes trailing slashes from environment base url', () => {
    import.meta.env.VITE_API_BASE_URL = 'https://api.example.com///';

    expect(getApiBaseUrl()).toBe('https://api.example.com');
    expect(getApiBaseUrl({ includeApiPath: true })).toBe('https://api.example.com/api');
  });

  it('falls back to window origin when environment base url is missing', () => {
    import.meta.env.VITE_API_BASE_URL = ' ';

    expect(getApiBaseUrl()).toBe(window.location.origin);
    expect(getApiBaseUrl({ includeApiPath: true })).toBe(`${window.location.origin}/api`);
  });

  it('reads and decodes cookie values', () => {
    document.cookie = 'token=plain-value';
    document.cookie = 'XSRF-TOKEN=encoded%20value';

    expect(readCookie('token')).toBe('plain-value');
    expect(readCookie('XSRF-TOKEN')).toBe('encoded value');
    expect(readCookie('missing')).toBeUndefined();
  });
});


