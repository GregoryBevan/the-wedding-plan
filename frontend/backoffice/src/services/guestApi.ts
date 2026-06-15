export interface CreateGuestPayload {
  firstName: string;
  lastName: string;
  email: string;
}

export const addGuest = async (payload: CreateGuestPayload) => {
  const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080';
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