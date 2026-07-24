import { getApiBaseUrl } from './http';

export interface GuestInvitationResponse {
  label: string;
  description: string;
  guests: GuestInvitationPerson[];
  guestCount: number;
}

export interface GuestInvitationPerson {
  id: string;
  firstName: string;
  lastName: string;
}

export class GuestAccessInvitationApiError extends Error {
  constructor(
    message: string,
    readonly status: number,
  ) {
    super(message);
    this.name = 'GuestAccessInvitationApiError';
  }
}

const guestAccessApiBaseUrl = getApiBaseUrl({ includeApiPath: true });

export const resolveInvitationByToken = async (token: string): Promise<GuestInvitationResponse> => {
  const response = await fetch(`${guestAccessApiBaseUrl}/guest-access/invitations/${encodeURIComponent(token)}`, {
    method: 'GET',
    headers: {
      Accept: 'application/json',
    },
  });

  if (!response.ok) {
    throw new GuestAccessInvitationApiError('Unable to resolve invitation.', response.status);
  }

  return response.json() as Promise<GuestInvitationResponse>;
};


