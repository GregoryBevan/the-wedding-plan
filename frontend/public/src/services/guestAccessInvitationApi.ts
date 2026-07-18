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

const normalizeBaseUrl = (value: string) => value.replace(/\/+$/, '');

const getApiBaseUrl = (): string => {
  const envBaseUrl = import.meta.env.VITE_API_BASE_URL?.trim();

  if (envBaseUrl) {
    return normalizeBaseUrl(envBaseUrl);
  }

  if (typeof window !== 'undefined' && window.location.origin) {
    if (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1') {
      return 'http://localhost:8080';
    }

    return normalizeBaseUrl(window.location.origin);
  }

  throw new Error('Missing API base URL configuration.');
};

const guestAccessApiBaseUrl = getApiBaseUrl();

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


