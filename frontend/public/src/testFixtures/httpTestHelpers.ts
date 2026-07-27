import { expect, vi } from 'vitest';

export const defaultCsrfToken = 'test-csrf-token';

type MockFetchResponseArgs = {
  ok: boolean;
  status?: number;
  body?: unknown;
  headers?: Record<string, string>;
};

export const setCsrfCookie = (token = defaultCsrfToken) => {
  document.cookie = `XSRF-TOKEN=${token}; path=/`;
};

export const clearCsrfCookie = () => {
  document.cookie = 'XSRF-TOKEN=; Max-Age=0; path=/';
};

export const mockFetchResponse = ({ ok, status = 200, body = {}, headers = {} }: MockFetchResponseArgs) => {
  return vi.spyOn(globalThis, 'fetch').mockResolvedValue({
    ok,
    status,
    headers: new Headers(headers),
    json: async () => body,
  } as Response);
};

export const getFirstRequest = (fetchMock: { mock: { calls: unknown[][] } }) => {
  return fetchMock.mock.calls[0] as [string, RequestInit];
};

export const expectCsrfHeader = (options: RequestInit, expectedToken = defaultCsrfToken) => {
  const headers = options.headers as Headers;
  expect(headers.get('X-XSRF-TOKEN')).toBe(expectedToken);
};

