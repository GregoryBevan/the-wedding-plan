import type { CreateGuestPayload, GuestPageResponse, GuestResponse } from '../services/guestApi';

export const createGuestPayload = (overrides: Partial<CreateGuestPayload> = {}): CreateGuestPayload => ({
  firstName: 'John',
  lastName: 'Doe',
  email: 'john.doe@email.com',
  ...overrides
});

export const createGuestResponse = (overrides: Partial<GuestResponse> = {}): GuestResponse => ({
  id: '1',
  version: 1,
  creationDate: '2026-06-23T10:00:00Z',
  updateDate: '2026-06-23T10:00:00Z',
  firstName: 'John',
  lastName: 'Doe',
  email: 'john.doe@email.com',
  ...overrides
});

interface CreateGuestPageOptions {
  items?: GuestResponse[];
  page?: number;
  size?: number;
  totalItems?: number;
  totalPages?: number;
}

export const createGuestPage = ({
  items = [],
  page = 0,
  size = 20,
  totalItems = items.length,
  totalPages = Math.max(1, Math.ceil(totalItems / size))
}: CreateGuestPageOptions = {}): GuestPageResponse => ({
  items,
  page,
  size,
  totalItems,
  totalPages
});

