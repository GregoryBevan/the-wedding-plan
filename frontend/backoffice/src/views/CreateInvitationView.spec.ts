import { flushPromises, mount } from '@vue/test-utils';
import { createMemoryHistory, createRouter } from 'vue-router';
import { defineComponent } from 'vue';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import CreateInvitationView from './CreateInvitationView.vue';
import { BACKOFFICE_ROUTE_NAMES } from '../router/routeNames';

const listGuestsMock = vi.hoisted(() => vi.fn());
const createInvitationMock = vi.hoisted(() => vi.fn());

vi.mock('../services/guestApi', async (importOriginal) => {
  const module = await importOriginal<typeof import('../services/guestApi')>();

  return {
    ...module,
    listGuests: listGuestsMock
  };
});

vi.mock('../services/invitationApi', async (importOriginal) => {
  const module = await importOriginal<typeof import('../services/invitationApi')>();

  return {
    ...module,
    createInvitation: createInvitationMock
  };
});

describe('CreateInvitationView', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  const mountView = async () => {
    const router = createRouter({
      history: createMemoryHistory(),
      routes: [
        {
          path: '/invitations/new',
          name: BACKOFFICE_ROUTE_NAMES.invitationAdd,
          component: CreateInvitationView
        },
        {
          path: '/invitations',
          name: BACKOFFICE_ROUTE_NAMES.invitationList,
          component: defineComponent({
            template: '<div>invitation list view</div>'
          })
        },
        {
          path: '/guests/new',
          name: BACKOFFICE_ROUTE_NAMES.guestAdd,
          component: defineComponent({
            template: '<div>create guest view</div>'
          })
        }
      ]
    });

    await router.push('/invitations/new');
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

  it('submits successfully and navigates back to invitation list', async () => {
    listGuestsMock.mockResolvedValue({
      items: [
        {
          id: 'guest-1',
          version: 1,
          creationDate: '2026-07-03T10:00:00Z',
          updateDate: '2026-07-03T10:00:00Z',
          firstName: 'Alice',
          lastName: 'Martin',
          email: 'alice@example.com'
        }
      ],
      page: 0,
      size: 10,
      totalItems: 1,
      totalPages: 1
    });
    createInvitationMock.mockResolvedValue({ id: 'inv-1' });

    const { wrapper, router } = await mountView();

    await wrapper.get('[data-test="invitation-label-input"]').setValue('Family table');
    await wrapper.get('[data-test="invitation-description-input"]').setValue('Main table');
    await wrapper.get('[data-test="guest-checkbox"]').setValue(true);
    await wrapper.get('form').trigger('submit.prevent');
    await flushPromises();

    expect(listGuestsMock).toHaveBeenCalledWith({ page: 0, size: 10, status: 'active', availability: 'unassigned', search: undefined });
    expect(createInvitationMock).toHaveBeenCalledWith({
      label: 'Family table',
      description: 'Main table',
      guestIds: ['guest-1']
    });
    expect(router.currentRoute.value.name).toBe(BACKOFFICE_ROUTE_NAMES.invitationList);
  });

  it('shows validation error when no guest is selected', async () => {
    listGuestsMock.mockResolvedValue({
      items: [
        {
          id: 'guest-1',
          version: 1,
          creationDate: '2026-07-03T10:00:00Z',
          updateDate: '2026-07-03T10:00:00Z',
          firstName: 'Alice',
          lastName: 'Martin',
          email: 'alice@example.com'
        }
      ],
      page: 0,
      size: 10,
      totalItems: 1,
      totalPages: 1
    });

    const { wrapper } = await mountView();

    await wrapper.get('[data-test="invitation-label-input"]').setValue('Family table');
    await wrapper.get('[data-test="invitation-description-input"]').setValue('Main table');
    await wrapper.get('form').trigger('submit.prevent');
    await flushPromises();

    expect(wrapper.get('[data-test="invitation-validation-error"]').text()).toContain('Select at least one guest.');
    expect(createInvitationMock).not.toHaveBeenCalled();
  });

  it('shows validation error when label is only whitespace', async () => {
    listGuestsMock.mockResolvedValue({
      items: [
        {
          id: 'guest-1',
          version: 1,
          creationDate: '2026-07-03T10:00:00Z',
          updateDate: '2026-07-03T10:00:00Z',
          firstName: 'Alice',
          lastName: 'Martin',
          email: 'alice@example.com'
        }
      ],
      page: 0,
      size: 10,
      totalItems: 1,
      totalPages: 1
    });

    const { wrapper } = await mountView();

    await wrapper.get('[data-test="invitation-label-input"]').setValue('   ');
    await wrapper.get('[data-test="guest-checkbox"]').setValue(true);
    await wrapper.get('form').trigger('submit.prevent');
    await flushPromises();

    expect(wrapper.get('[data-test="invitation-validation-error"]').text()).toContain('Label is required.');
    expect(createInvitationMock).not.toHaveBeenCalled();
  });

  it('shows api error when submit fails', async () => {
    listGuestsMock.mockResolvedValue({
      items: [
        {
          id: 'guest-1',
          version: 1,
          creationDate: '2026-07-03T10:00:00Z',
          updateDate: '2026-07-03T10:00:00Z',
          firstName: 'Alice',
          lastName: 'Martin',
          email: 'alice@example.com'
        }
      ],
      page: 0,
      size: 10,
      totalItems: 1,
      totalPages: 1
    });
    createInvitationMock.mockRejectedValue(new Error('At least one guest is required.'));

    const { wrapper, router } = await mountView();

    await wrapper.get('[data-test="invitation-label-input"]').setValue('Family table');
    await wrapper.get('[data-test="guest-checkbox"]').setValue(true);
    await wrapper.get('form').trigger('submit.prevent');
    await flushPromises();

    expect(wrapper.get('[data-test="invitation-submit-error"]').text()).toContain('At least one guest is required.');
    expect(router.currentRoute.value.name).toBe(BACKOFFICE_ROUTE_NAMES.invitationAdd);
  });

  it('shows clear conflict error when a selected guest has been assigned concurrently', async () => {
    listGuestsMock.mockResolvedValue({
      items: [
        {
          id: 'guest-1',
          version: 1,
          creationDate: '2026-07-03T10:00:00Z',
          updateDate: '2026-07-03T10:00:00Z',
          firstName: 'Alice',
          lastName: 'Martin',
          email: 'alice@example.com'
        }
      ],
      page: 0,
      size: 10,
      totalItems: 1,
      totalPages: 1
    });
    createInvitationMock.mockRejectedValue(new Error('Some guests are already assigned to another invitation.'));

    const { wrapper, router } = await mountView();

    await wrapper.get('[data-test="invitation-label-input"]').setValue('Family table');
    await wrapper.get('[data-test="guest-checkbox"]').setValue(true);
    await wrapper.get('form').trigger('submit.prevent');
    await flushPromises();

    expect(wrapper.get('[data-test="invitation-submit-error"]').text()).toContain('Some guests are already assigned to another invitation.');
    expect(router.currentRoute.value.name).toBe(BACKOFFICE_ROUTE_NAMES.invitationAdd);
  });

  it('shows empty available guests state with create guest link', async () => {
    listGuestsMock.mockResolvedValue({
      items: [],
      page: 0,
      size: 10,
      totalItems: 0,
      totalPages: 0
    });

    const { wrapper } = await mountView();

    expect(wrapper.get('[data-test="empty-no-guests-available"]').text()).toContain('No guests available yet.');
    expect(wrapper.get('[data-test="create-guest-link"]').attributes('href')).toBe('/guests/new');
    expect(wrapper.find('[data-test="empty-no-search-match"]').exists()).toBe(false);
  });

  it('shows no search match message when backend search has no result', async () => {
    vi.useFakeTimers();

    try {
      listGuestsMock
        .mockResolvedValueOnce({
          items: [
            {
              id: 'guest-1',
              version: 1,
              creationDate: '2026-07-03T10:00:00Z',
              updateDate: '2026-07-03T10:00:00Z',
              firstName: 'Alice',
              lastName: 'Martin',
              email: 'alice@example.com'
            }
          ],
          page: 0,
          size: 10,
          totalItems: 1,
          totalPages: 1
        })
        .mockResolvedValueOnce({
          items: [],
          page: 0,
          size: 10,
          totalItems: 0,
          totalPages: 0
        });

      const { wrapper } = await mountView();

      await wrapper.get('[data-test="guest-search-input"]').setValue('zzz-does-not-match');
      vi.advanceTimersByTime(301);
      await flushPromises();

      expect(listGuestsMock).toHaveBeenNthCalledWith(2, { page: 0, size: 10, status: 'active', availability: 'unassigned', search: 'zzz-does-not-match' });
      expect(wrapper.get('[data-test="empty-no-search-match"]').text()).toContain('No guests match your search.');
      expect(wrapper.find('[data-test="empty-no-guests-available"]').exists()).toBe(false);
    } finally {
      vi.useRealTimers();
    }
  });

  it('loads more guests when scrolling near the bottom of the list', async () => {
    listGuestsMock
      .mockResolvedValueOnce({
        items: [
          {
            id: 'guest-1',
            version: 1,
            creationDate: '2026-07-03T10:00:00Z',
            updateDate: '2026-07-03T10:00:00Z',
            firstName: 'Alice',
            lastName: 'Martin',
            email: 'alice@example.com'
          }
        ],
        page: 0,
        size: 10,
        totalItems: 11,
        totalPages: 2
      })
      .mockResolvedValueOnce({
        items: [
          {
            id: 'guest-11',
            version: 1,
            creationDate: '2026-07-03T10:00:00Z',
            updateDate: '2026-07-03T10:00:00Z',
            firstName: 'Zoe',
            lastName: 'Durand',
            email: 'zoe@example.com'
          }
        ],
        page: 1,
        size: 10,
        totalItems: 11,
        totalPages: 2
      });

    const { wrapper } = await mountView();

    const container = wrapper.get('[data-test="guest-options-container"]');
    const containerElement = container.element as HTMLElement;

    Object.defineProperty(containerElement, 'scrollTop', { configurable: true, value: 160, writable: true });
    Object.defineProperty(containerElement, 'clientHeight', { configurable: true, value: 120 });
    Object.defineProperty(containerElement, 'scrollHeight', { configurable: true, value: 280 });

    await container.trigger('scroll');
    await flushPromises();

    expect(listGuestsMock).toHaveBeenNthCalledWith(1, { page: 0, size: 10, status: 'active', availability: 'unassigned', search: undefined });
    expect(listGuestsMock).toHaveBeenNthCalledWith(2, { page: 1, size: 10, status: 'active', availability: 'unassigned', search: undefined });
    expect(wrapper.text()).toContain('Alice Martin');
    expect(wrapper.text()).toContain('Zoe Durand');
  });
});

