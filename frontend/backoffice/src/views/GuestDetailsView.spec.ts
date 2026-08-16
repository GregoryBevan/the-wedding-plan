import { flushPromises, mount } from '@vue/test-utils';
import { createMemoryHistory, createRouter } from 'vue-router';
import { defineComponent } from 'vue';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import GuestDetailsView from './GuestDetailsView.vue';
import { BACKOFFICE_ROUTE_NAMES } from '../router/routeNames';
import { applyCapabilities, resetCapabilities } from '../composables/useCapabilities';

const dateTimeFormatter = new Intl.DateTimeFormat(undefined, {
  dateStyle: 'medium',
  timeStyle: 'short'
});

const getGuestByIdMock = vi.hoisted(() => vi.fn());

vi.mock('../services/guestApi', () => ({
  getGuestById: getGuestByIdMock
}));

describe('GuestDetailsView', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    applyCapabilities({ canWrite: true });
  });

  afterEach(() => {
    resetCapabilities();
  });

  const mountView = async ({
    initialPath = '/guests/guest-1',
    previousPath
  }: {
    initialPath?: string;
    previousPath?: string;
  } = {}) => {
    const router = createRouter({
      history: createMemoryHistory(),
      routes: [
        {
          path: '/guests',
          name: BACKOFFICE_ROUTE_NAMES.guestList,
          component: defineComponent({
            template: '<div>guests list view</div>'
          })
        },
        {
          path: '/guests/:id/edit',
          name: BACKOFFICE_ROUTE_NAMES.guestEdit,
          component: defineComponent({
            template: '<div>edit guest view</div>'
          })
        },
        {
          path: '/guests/:id',
          name: BACKOFFICE_ROUTE_NAMES.guestDetails,
          component: GuestDetailsView
        },
        {
          path: '/guests-without-id',
          component: GuestDetailsView
        }
      ]
    });

    if (previousPath) {
      await router.push(previousPath);
      await router.isReady();
    }

    await router.push(initialPath);
    await router.isReady();

    const wrapper = mount(defineComponent({
      template: '<RouterView />'
    }), {
      global: {
        plugins: [router]
      }
    });

    await flushPromises();

    return { wrapper, router };
  };

  it('navigates back in history when clicking back button', async () => {
    getGuestByIdMock.mockResolvedValue({
      id: 'guest-1',
      version: 1,
      creationDate: '2026-07-03T10:00:00Z',
      updateDate: '2026-07-04T10:00:00Z',
      firstName: 'Alice',
      lastName: 'Martin',
      email: 'alice@example.com',
      language: 'FR'
    });

    const { wrapper, router } = await mountView({ previousPath: '/guests' });
    const backSpy = vi.spyOn(router, 'back');

    window.history.replaceState({ back: '/guests' }, '');

    await wrapper.get('[data-test="back-guest-details"]').trigger('click');
    await flushPromises();

    expect(backSpy).toHaveBeenCalled();
    expect(router.currentRoute.value.path).toBe('/guests');
  });

  it('falls back to the guest list when there is no history to go back to', async () => {
    getGuestByIdMock.mockResolvedValue({
      id: 'guest-1',
      version: 1,
      creationDate: '2026-07-03T10:00:00Z',
      updateDate: '2026-07-04T10:00:00Z',
      firstName: 'Alice',
      lastName: 'Martin',
      email: 'alice@example.com',
      language: 'FR'
    });

    const { wrapper, router } = await mountView();

    window.history.replaceState({}, '');

    await wrapper.get('[data-test="back-guest-details"]').trigger('click');
    await flushPromises();

    expect(router.currentRoute.value.name).toBe(BACKOFFICE_ROUTE_NAMES.guestList);
  });

  it('shows a not found error and skips the request when the id is missing', async () => {
    const { wrapper } = await mountView({ initialPath: '/guests-without-id' });

    expect(getGuestByIdMock).not.toHaveBeenCalled();
    expect(wrapper.get('[data-test="guest-details-error"]').text()).toBe('Guest not found.');
    expect(wrapper.find('[data-test="guest-details-card"]').exists()).toBe(false);
  });

  it('renders guest details', async () => {
    getGuestByIdMock.mockResolvedValue({
      id: 'guest-1',
      version: 1,
      creationDate: '2026-07-03T10:00:00Z',
      updateDate: '2026-07-04T10:00:00Z',
      firstName: 'Alice',
      lastName: 'Martin',
      email: 'alice@example.com',
      language: 'EN'
    });

    const { wrapper } = await mountView();

    expect(getGuestByIdMock).toHaveBeenCalledWith('guest-1');
    expect(wrapper.get('[data-test="guest-details-card"]')).toBeDefined();
    expect(wrapper.get('[data-test="guest-details-name"]').text()).toBe('Alice Martin');
    expect(wrapper.get('[data-test="guest-details-email"]').text()).toBe('alice@example.com');
    expect(wrapper.get('[data-test="guest-details-language"]').text()).toBe('English');
    expect(wrapper.get('[data-test="guest-details-creation-date"]').text()).toBe(
      dateTimeFormatter.format(new Date('2026-07-03T10:00:00Z'))
    );
    expect(wrapper.get('[data-test="guest-details-update-date"]').text()).toBe(
      dateTimeFormatter.format(new Date('2026-07-04T10:00:00Z'))
    );
  });

  it('renders error state when loading guest fails', async () => {
    getGuestByIdMock.mockRejectedValue(new Error('Guest not found.'));

    const { wrapper } = await mountView({ initialPath: '/guests/guest-404' });

    expect(wrapper.get('[data-test="guest-details-error"]').text()).toBe('Guest not found.');
    expect(wrapper.find('[data-test="guest-details-card"]').exists()).toBe(false);
  });

  it('renders a generic error message when the failure is not an Error', async () => {
    getGuestByIdMock.mockRejectedValue('boom');

    const { wrapper } = await mountView();

    expect(wrapper.get('[data-test="guest-details-error"]').text()).toBe('Unexpected error while loading guest details.');
  });

  it('shows the edit link for users with write access and preserves pagination query', async () => {
    getGuestByIdMock.mockResolvedValue({
      id: 'guest-1',
      version: 1,
      creationDate: '2026-07-03T10:00:00Z',
      updateDate: '2026-07-04T10:00:00Z',
      firstName: 'Alice',
      lastName: 'Martin',
      email: 'alice@example.com',
      language: 'FR'
    });

    const { wrapper } = await mountView({ initialPath: '/guests/guest-1?page=2&size=10' });

    expect(wrapper.get('[data-test="edit-guest-link"]').attributes('href')).toBe('/guests/guest-1/edit?page=2&size=10');
  });

  it('hides the edit link in read-only mode', async () => {
    resetCapabilities();
    getGuestByIdMock.mockResolvedValue({
      id: 'guest-1',
      version: 1,
      creationDate: '2026-07-03T10:00:00Z',
      updateDate: '2026-07-04T10:00:00Z',
      firstName: 'Alice',
      lastName: 'Martin',
      email: 'alice@example.com',
      language: 'FR'
    });

    const { wrapper } = await mountView();

    expect(wrapper.find('[data-test="edit-guest-link"]').exists()).toBe(false);
  });
});





