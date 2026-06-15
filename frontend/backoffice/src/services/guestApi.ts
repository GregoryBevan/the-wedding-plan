export interface CreateGuestPayload {
  firstName: string;
  lastName: string;
  email: string;
}

export const createGuest = async (payload: CreateGuestPayload) => {
  const response = await fetch('http://localhost:8080/guests', {
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