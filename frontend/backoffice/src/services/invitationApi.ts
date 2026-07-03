import { getApiBaseUrl, readCookie } from './http';

export interface InvitationResponse {
  id: string;
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



