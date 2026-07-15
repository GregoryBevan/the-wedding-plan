import { getApiBaseUrl, readCookie } from './http';

export interface InvitationResponse {
  id: string;
  version: number;
  creationDate: string;
  updateDate: string;
  label: string;
  description: string;
  guests: InvitationGuestResponse[];
  guestCount: number;
}

export interface InvitationGuestResponse {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
}

export interface InvitationPageResponse {
  items: InvitationResponse[];
  page: number;
  size: number;
  totalItems: number;
  totalPages: number;
}

export interface CreateInvitationPayload {
  label: string;
  description: string;
  guestIds: string[];
}

export interface UpdateInvitationPayload {
  version: number;
  label: string;
  description: string;
  guestIds: string[];
}

export const getInvitationById = async (id: string): Promise<InvitationResponse> => {
  const csrfToken = readCookie('XSRF-TOKEN');
  const headers = new Headers();

  if (csrfToken) {
    headers.set('X-XSRF-TOKEN', csrfToken);
  }

  const response = await fetch(`${invitationApiBaseUrl}/invitations/${id}`, {
    method: 'GET',
    credentials: 'include',
    headers
  });

  if (!response.ok) {
    if (response.status === 404) {
      throw new Error('Invitation not found.');
    }

    throw new Error('Unable to retrieve invitation details at the moment.');
  }

  return response.json() as Promise<InvitationResponse>;
};

const invitationApiBaseUrl = getApiBaseUrl({ includeApiPath: true });

export const listInvitations = async (
  { page = 0, size = 20 }: { page?: number; size?: number } = {}
): Promise<InvitationPageResponse> => {
  const csrfToken = readCookie('XSRF-TOKEN');
  const headers = new Headers();

  if (csrfToken) {
    headers.set('X-XSRF-TOKEN', csrfToken);
  }

  const queryParams = new URLSearchParams({
    page: String(page),
    size: String(size)
  });

  const response = await fetch(`${invitationApiBaseUrl}/invitations?${queryParams}`, {
    method: 'GET',
    credentials: 'include',
    headers
  });

  if (!response.ok) {
    throw new Error('Unable to retrieve invitations at the moment.');
  }

  return response.json() as Promise<InvitationPageResponse>;
};

export const createInvitation = async (payload: CreateInvitationPayload): Promise<InvitationResponse> => {
  const csrfToken = readCookie('XSRF-TOKEN');
  const headers = new Headers({
    'Content-Type': 'application/json'
  });

  if (csrfToken) {
    headers.set('X-XSRF-TOKEN', csrfToken);
  }

  const response = await fetch(`${invitationApiBaseUrl}/invitations`, {
    method: 'POST',
    credentials: 'include',
    headers,
    body: JSON.stringify(payload)
  });

  if (!response.ok) {
    const body = await response.json().catch(() => null) as { message?: string } | null;

    if (response.status === 409) {
      throw new Error(body?.message ?? 'Some guests are already assigned to another invitation. Please refresh and try again.');
    }

    throw new Error(body?.message ?? 'Unable to create invitation at the moment.');
  }

  return response.json() as Promise<InvitationResponse>;
};

export const updateInvitation = async (id: string, payload: UpdateInvitationPayload): Promise<InvitationResponse> => {
  const csrfToken = readCookie('XSRF-TOKEN');
  const headers = new Headers({
    'Content-Type': 'application/json'
  });

  if (csrfToken) {
    headers.set('X-XSRF-TOKEN', csrfToken);
  }

  const response = await fetch(`${invitationApiBaseUrl}/invitations/${id}`, {
    method: 'PUT',
    credentials: 'include',
    headers,
    body: JSON.stringify(payload)
  });

  if (!response.ok) {
    const body = await response.json().catch(() => null) as { message?: string } | null;

    if (response.status === 404) {
      throw new Error(body?.message ?? 'Invitation not found.');
    }

    if (response.status === 400) {
      throw new Error(body?.message ?? 'Invalid invitation data.');
    }

    if (response.status === 409) {
      throw new Error(body?.message ?? 'This invitation has been modified elsewhere. Please reload and try again.');
    }

    throw new Error(body?.message ?? 'Unable to update invitation at the moment.');
  }

  return response.json() as Promise<InvitationResponse>;
};



