import { flushPromises, mount } from '@vue/test-utils';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import GuestList from './GuestList.vue';
import { createGuestPage, createGuestResponse } from '../testFixtures/guestFixtures';

const listGuestsMock = vi.hoisted(() => vi.fn());

vi.mock('../services/guestApi', () => ({
  listGuests: listGuestsMock
}));

describe('GuestList', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('loads and renders guests on mount', async () => {
    listGuestsMock.mockResolvedValue(createGuestPage({
      items: [createGuestResponse()],
      totalItems: 1,
      totalPages: 1
    }));

    const wrapper = mount(GuestList);

    await flushPromises();

    expect(listGuestsMock).toHaveBeenCalledWith({ page: 0, size: 20 });
    expect(wrapper.text()).toContain('John Doe');
    expect(wrapper.text()).toContain('john.doe@email.com');
  });

  it('requests next page when clicking next button', async () => {
    listGuestsMock
      .mockResolvedValueOnce(createGuestPage({
        items: [createGuestResponse()],
        page: 0,
        totalItems: 30,
        totalPages: 2
      }))
      .mockResolvedValueOnce(createGuestPage({
        items: [createGuestResponse({
          id: '2',
          creationDate: '2026-06-23T11:00:00Z',
          updateDate: '2026-06-23T11:00:00Z',
          firstName: 'Jane',
          email: 'jane.doe@email.com'
        })],
        page: 1,
        totalItems: 30,
        totalPages: 2
      }));

    const wrapper = mount(GuestList);

    await flushPromises();

    const nextButton = wrapper.findAll('button').find((button) => button.text().includes('Next'));
    expect(nextButton).toBeDefined();
    await nextButton!.trigger('click');
    await flushPromises();

    expect(listGuestsMock).toHaveBeenNthCalledWith(2, { page: 1, size: 20 });
    expect(wrapper.text()).toContain('Jane Doe');
  });

  it('requests previous page when clicking previous button', async () => {
    listGuestsMock
      .mockResolvedValueOnce(createGuestPage({
        items: [createGuestResponse({
          id: '2',
          creationDate: '2026-06-23T11:00:00Z',
          updateDate: '2026-06-23T11:00:00Z',
          firstName: 'Jane',
          email: 'jane.doe@email.com'
        })],
        page: 1,
        totalItems: 30,
        totalPages: 2
      }))
      .mockResolvedValueOnce(createGuestPage({
        items: [createGuestResponse()],
        page: 0,
        totalItems: 30,
        totalPages: 2
      }));

    const wrapper = mount(GuestList);

    await flushPromises();

    const previousButton = wrapper.findAll('button').find((button) => button.text().includes('Previous'));
    expect(previousButton).toBeDefined();
    await previousButton!.trigger('click');
    await flushPromises();

    expect(listGuestsMock).toHaveBeenNthCalledWith(2, { page: 0, size: 20 });
    expect(wrapper.text()).toContain('John Doe');
  });

  it('shows error message and retry button when loading fails', async () => {
    listGuestsMock.mockRejectedValue(new Error('Network error'));

    const wrapper = mount(GuestList);

    await flushPromises();

    expect(wrapper.text()).toContain('Network error');
    expect(wrapper.findAll('button').find((button) => button.text().includes('Try again'))).toBeDefined();
  });
});
