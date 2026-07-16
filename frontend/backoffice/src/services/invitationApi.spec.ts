import { afterEach, describe, expect, it, vi } from 'vitest';
import { createInvitation, getInvitationById, listInvitations, updateInvitation } from './invitationApi';
import { clearCsrfCookie, expectCsrfHeader, getFirstRequest, mockFetchResponse, setCsrfCookie } from '../testFixtures/httpTestHelpers';

const invitationNotFoundMessage = 'Invitation not found.';
const invitationConflictMessage = 'Some guests are already assigned to another invitation. Please refresh and try again.';
const invitationVersionConflictMessage = 'This invitation has been modified elsewhere. Please reload and try again.';
const invitationResponse = {
  id: 'inv-1',
  accessToken: 'token-inv-1',
  version: 1,
  creationDate: '2026-07-03T10:00:00Z',
  updateDate: '2026-07-03T10:00:00Z',
  label: 'Family table',
  description: 'Main family table',
  guests: [],
  guestCount: 2
};

const updatedInvitationResponse = {
  ...invitationResponse,
  version: 1,
  label: 'Updated Family table',
  description: 'Updated main family table',
  updateDate: '2026-07-04T10:00:00Z'
};

const createInvitationPayload = {
  label: 'Family table',
  description: 'Main family table',
  guestIds: ['guest-1', 'guest-2']
};

const updateInvitationPayload = {
  version: 1,
  label: 'Updated Family table',
  description: 'Updated main family table',
  guestIds: ['guest-1', 'guest-2']
};

describe('invitationApi', () => {
  afterEach(() => {
    vi.restoreAllMocks();
    clearCsrfCookie();
  });

  it('calls backend invitations list endpoint with pagination and csrf header', async () => {
    setCsrfCookie();

    const fetchMock = mockFetchResponse({
      ok: true,
      body: {
        items: [],
        page: 0,
        size: 20,
        totalItems: 0,
        totalPages: 0
      }
    });

    await listInvitations({ page: 1, size: 10 });

    const [url, options] = getFirstRequest(fetchMock);

    expect(url).toBe('http://localhost:8080/api/invitations?page=1&size=10');
    expect(options).toMatchObject({
      method: 'GET',
      credentials: 'include'
    });

    expectCsrfHeader(options);
  });

  it('throws when list endpoint response is not ok', async () => {
    mockFetchResponse({ ok: false });

    await expect(listInvitations()).rejects.toThrow('Unable to retrieve invitations at the moment.');
  });

  it('calls backend invitation details endpoint with csrf header', async () => {
    setCsrfCookie();

    const fetchMock = mockFetchResponse({ ok: true, body: invitationResponse });

    await getInvitationById('inv-1');

    const [url, options] = getFirstRequest(fetchMock);

    expect(url).toBe('http://localhost:8080/api/invitations/inv-1');
    expect(options).toMatchObject({
      method: 'GET',
      credentials: 'include'
    });

    expectCsrfHeader(options);
  });

  it('maps snake_case access token from backend response', async () => {
    mockFetchResponse({
      ok: true,
      body: {
        ...invitationResponse,
        accessToken: undefined,
        access_token: 'token-from-snake-case'
      }
    });

    const invitation = await getInvitationById('inv-1');

    expect(invitation.accessToken).toBe('token-from-snake-case');
  });

  it('maps lowercase accesstoken from backend response', async () => {
    mockFetchResponse({
      ok: true,
      body: {
        ...invitationResponse,
        accessToken: undefined,
        accesstoken: 'token-from-lowercase-field'
      }
    });

    const invitation = await getInvitationById('inv-1');

    expect(invitation.accessToken).toBe('token-from-lowercase-field');
  });

  it('throws not found message when details endpoint returns 404', async () => {
    mockFetchResponse({ ok: false, status: 404 });

    await expect(getInvitationById('inv-404')).rejects.toThrow(invitationNotFoundMessage);
  });

  it('calls backend create invitation endpoint with csrf header', async () => {
    setCsrfCookie();

    const fetchMock = mockFetchResponse({ ok: true, body: invitationResponse });

    await createInvitation(createInvitationPayload);

    const [url, options] = getFirstRequest(fetchMock);

    expect(url).toBe('http://localhost:8080/api/invitations');
    expect(options).toMatchObject({
      method: 'POST',
      credentials: 'include',
      body: JSON.stringify(createInvitationPayload)
    });

    const headers = options.headers as Headers;
    expect(headers.get('Content-Type')).toBe('application/json');
    expectCsrfHeader(options);
  });

  it('throws api message when create endpoint response is not ok', async () => {
    mockFetchResponse({
      ok: false,
      body: { message: 'At least one guest is required.' }
    });

    await expect(createInvitation({
      label: 'Family table',
      description: 'Main family table',
      guestIds: []
    })).rejects.toThrow('At least one guest is required.');
  });

  it('throws dedicated conflict message when create endpoint returns 409 without body message', async () => {
    mockFetchResponse({ ok: false, status: 409 });

    await expect(createInvitation({
      label: 'Family table',
      description: 'Main family table',
      guestIds: ['guest-1']
    })).rejects.toThrow(invitationConflictMessage);
  });

  it('calls backend update invitation endpoint with csrf header', async () => {
    setCsrfCookie();

    const fetchMock = mockFetchResponse({ ok: true, body: updatedInvitationResponse });

    await updateInvitation('inv-1', updateInvitationPayload);

    const [url, options] = getFirstRequest(fetchMock);

    expect(url).toBe('http://localhost:8080/api/invitations/inv-1');
    expect(options).toMatchObject({
      method: 'PUT',
      credentials: 'include',
      body: JSON.stringify(updateInvitationPayload)
    });

    const headers = options.headers as Headers;
    expect(headers.get('Content-Type')).toBe('application/json');
    expectCsrfHeader(options);
  });

  it('throws not found message when update endpoint returns 404', async () => {
    mockFetchResponse({ ok: false, status: 404 });

    await expect(updateInvitation('inv-404', {
      version: 1,
      label: 'Updated Family table',
      description: 'Updated main family table',
      guestIds: ['guest-1']
    })).rejects.toThrow(invitationNotFoundMessage);
  });

  it('throws version conflict message when update endpoint returns 409 without body message', async () => {
    mockFetchResponse({ ok: false, status: 409 });

    await expect(updateInvitation('inv-1', {
      version: 1,
      label: 'Updated Family table',
      description: 'Updated main family table',
      guestIds: ['guest-1']
    })).rejects.toThrow(invitationVersionConflictMessage);
  });
});

