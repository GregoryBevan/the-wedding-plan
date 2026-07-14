import { afterEach, describe, expect, it, vi } from 'vitest';
import { createInvitation, getInvitationById, listInvitations, updateInvitation } from './invitationApi';

describe('invitationApi', () => {
  afterEach(() => {
    vi.restoreAllMocks();
    document.cookie = 'XSRF-TOKEN=; Max-Age=0; path=/';
  });

  it('calls backend invitations list endpoint with pagination and csrf header', async () => {
    document.cookie = 'XSRF-TOKEN=test-csrf-token';

    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: true,
      json: async () => ({
        items: [],
        page: 0,
        size: 20,
        totalItems: 0,
        totalPages: 0
      })
    } as Response);

    await listInvitations({ page: 1, size: 10 });

    const [url, options] = fetchMock.mock.calls[0] as [string, RequestInit];

    expect(url).toBe('http://localhost:8080/api/invitations?page=1&size=10');
    expect(options).toMatchObject({
      method: 'GET',
      credentials: 'include'
    });

    const headers = options.headers as Headers;
    expect(headers.get('X-XSRF-TOKEN')).toBe('test-csrf-token');
  });

  it('throws when list endpoint response is not ok', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: false,
      json: async () => ({})
    } as Response);

    await expect(listInvitations()).rejects.toThrow('Unable to retrieve invitations at the moment.');
  });

  it('calls backend invitation details endpoint with csrf header', async () => {
    document.cookie = 'XSRF-TOKEN=test-csrf-token';

    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: true,
      json: async () => ({
        id: 'inv-1',
        creationDate: '2026-07-03T10:00:00Z',
        updateDate: '2026-07-03T10:00:00Z',
        label: 'Family table',
        description: 'Main family table',
        guests: [],
        guestCount: 2
      })
    } as Response);

    await getInvitationById('inv-1');

    const [url, options] = fetchMock.mock.calls[0] as [string, RequestInit];

    expect(url).toBe('http://localhost:8080/api/invitations/inv-1');
    expect(options).toMatchObject({
      method: 'GET',
      credentials: 'include'
    });

    const headers = options.headers as Headers;
    expect(headers.get('X-XSRF-TOKEN')).toBe('test-csrf-token');
  });

  it('throws not found message when details endpoint returns 404', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: false,
      status: 404,
      json: async () => ({})
    } as Response);

    await expect(getInvitationById('inv-404')).rejects.toThrow('Invitation not found.');
  });

  it('calls backend create invitation endpoint with csrf header', async () => {
    document.cookie = 'XSRF-TOKEN=test-csrf-token';

    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: true,
      json: async () => ({
        id: 'inv-1',
        creationDate: '2026-07-03T10:00:00Z',
        updateDate: '2026-07-03T10:00:00Z',
        label: 'Family table',
        description: 'Main family table',
        guests: [],
        guestCount: 2
      })
    } as Response);

    await createInvitation({
      label: 'Family table',
      description: 'Main family table',
      guestIds: ['guest-1', 'guest-2']
    });

    const [url, options] = fetchMock.mock.calls[0] as [string, RequestInit];

    expect(url).toBe('http://localhost:8080/api/invitations');
    expect(options).toMatchObject({
      method: 'POST',
      credentials: 'include',
      body: JSON.stringify({
        label: 'Family table',
        description: 'Main family table',
        guestIds: ['guest-1', 'guest-2']
      })
    });

    const headers = options.headers as Headers;
    expect(headers.get('Content-Type')).toBe('application/json');
    expect(headers.get('X-XSRF-TOKEN')).toBe('test-csrf-token');
  });

  it('throws api message when create endpoint response is not ok', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: false,
      json: async () => ({ message: 'At least one guest is required.' })
    } as Response);

    await expect(createInvitation({
      label: 'Family table',
      description: 'Main family table',
      guestIds: []
    })).rejects.toThrow('At least one guest is required.');
  });

  it('throws dedicated conflict message when create endpoint returns 409 without body message', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: false,
      status: 409,
      json: async () => ({})
    } as Response);

    await expect(createInvitation({
      label: 'Family table',
      description: 'Main family table',
      guestIds: ['guest-1']
    })).rejects.toThrow('Some guests are already assigned to another invitation. Please refresh and try again.');
  });

  it('calls backend update invitation endpoint with csrf header', async () => {
    document.cookie = 'XSRF-TOKEN=test-csrf-token';

    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: true,
      json: async () => ({
        id: 'inv-1',
        creationDate: '2026-07-03T10:00:00Z',
        updateDate: '2026-07-04T10:00:00Z',
        label: 'Updated Family table',
        description: 'Updated main family table',
        guests: [],
        guestCount: 2
      })
    } as Response);

    await updateInvitation('inv-1', {
      label: 'Updated Family table',
      description: 'Updated main family table',
      guestIds: ['guest-1', 'guest-2']
    });

    const [url, options] = fetchMock.mock.calls[0] as [string, RequestInit];

    expect(url).toBe('http://localhost:8080/api/invitations/inv-1');
    expect(options).toMatchObject({
      method: 'PUT',
      credentials: 'include',
      body: JSON.stringify({
        label: 'Updated Family table',
        description: 'Updated main family table',
        guestIds: ['guest-1', 'guest-2']
      })
    });

    const headers = options.headers as Headers;
    expect(headers.get('Content-Type')).toBe('application/json');
    expect(headers.get('X-XSRF-TOKEN')).toBe('test-csrf-token');
  });

  it('throws not found message when update endpoint returns 404', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: false,
      status: 404,
      json: async () => ({})
    } as Response);

    await expect(updateInvitation('inv-404', {
      label: 'Updated Family table',
      description: 'Updated main family table',
      guestIds: ['guest-1']
    })).rejects.toThrow('Invitation not found.');
  });

  it('throws dedicated conflict message when update endpoint returns 409 without body message', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: false,
      status: 409,
      json: async () => ({})
    } as Response);

    await expect(updateInvitation('inv-1', {
      label: 'Updated Family table',
      description: 'Updated main family table',
      guestIds: ['guest-1']
    })).rejects.toThrow('Some guests are already assigned to another invitation. Please refresh and try again.');
  });
});

