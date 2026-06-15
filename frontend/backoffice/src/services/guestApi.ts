export interface CreateGuestPayload {
  firstName: string;
  lastName: string;
  email: string;
}

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL;

if (!apiBaseUrl) {
  throw new Error('Missing VITE_API_BASE_URL environment variable.');
}

export const addGuest = async (payload: CreateGuestPayload) => {
  const response = await fetch(`${apiBaseUrl}/guests`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(payload)
  });

  if (!response.ok) {
    throw new Error('Unable to create guest at the moment.');
  }

  return response.json();
};