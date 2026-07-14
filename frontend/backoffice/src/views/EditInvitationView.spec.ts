import { flushPromises, mount } from '@vue/test-utils';
import { createMemoryHistory, createRouter } from 'vue-router';
import { defineComponent } from 'vue';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import EditInvitationView from './EditInvitationView.vue';
import { BACKOFFICE_ROUTE_NAMES } from '../router/routeNames';

const listGuestsMock = vi.hoisted(() => vi.fn());
const getInvitationByIdMock = vi.hoisted(() => vi.fn());
const updateInvitationMock = vi.hoisted(() => vi.fn());

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
    getInvitationById: getInvitationByIdMock,
    updateInvitation: updateInvitationMock
  };
});

describe('EditInvitationView', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  const mountView = async (invitationId = 'inv-1') => {
    const router = createRouter({
      history: createMemoryHistory(),
      routes: [
        {
          path: '/invitations/:id/edit',
          name: BACKOFFICE_ROUTE_NAMES.invitationEdit,
          component: EditInvitationView
        },
        {
          path: '/invitations/:id',
          name: BACKOFFICE_ROUTE_NAMES.invitationDetails,
          component: defineComponent({
            template: '<div>invitation details view</div>'
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

    await router.push(`/invitations/${invitationId}/edit`);
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

  it('loads existing invitation data and allows editing', async () => {
    getInvitationByIdMock.mockResolvedValue({
      id: 'inv-1',
      version: 1,
      creationDate: '2026-07-03T10:00:00Z',
      updateDate: '2026-07-03T10:00:00Z',
      label: 'Family table',
      description: 'Main family table',
      guests: [
        {
          id: 'guest-1',
          firstName: 'Alice',
          lastName: 'Martin',
          email: 'alice@example.com'
        },
        {
          id: 'guest-2',
          firstName: 'Bob',
          lastName: 'Durand',
          email: 'bob@example.com'
        }
      ],
      guestCount: 2
    });

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
        },
        {
          id: 'guest-2',
          version: 1,
          creationDate: '2026-07-03T10:00:00Z',
          updateDate: '2026-07-03T10:00:00Z',
          firstName: 'Bob',
          lastName: 'Durand',
          email: 'bob@example.com'
        }
      ],
      page: 0,
      size: 20,
      totalItems: 2,
      totalPages: 1
    });

    updateInvitationMock.mockResolvedValue({
      id: 'inv-1',
      version: 1,
      creationDate: '2026-07-03T10:00:00Z',
      updateDate: '2026-07-04T10:00:00Z',
      label: 'Family table updated',
      description: 'Updated family table',
      guests: [
        {
          id: 'guest-1',
          firstName: 'Alice',
          lastName: 'Martin',
          email: 'alice@example.com'
        }
      ],
      guestCount: 1
    });

    const { wrapper, router } = await mountView();

    expect(getInvitationByIdMock).toHaveBeenCalledWith('inv-1');
    expect((wrapper.get('[data-test="invitation-label-input"]').element as HTMLInputElement).value).toBe('Family table');
    expect((wrapper.get('[data-test="invitation-description-input"]').element as HTMLTextAreaElement).value).toBe('Main family table');

    const checkboxes = wrapper.findAll('[data-test="guest-checkbox"]');
    expect(checkboxes).toHaveLength(2);
    expect((checkboxes[0].element as HTMLInputElement).checked).toBe(true);
    expect((checkboxes[1].element as HTMLInputElement).checked).toBe(true);

    await wrapper.get('[data-test="invitation-label-input"]').setValue('Family table updated');
    await wrapper.get('[data-test="invitation-description-input"]').setValue('Updated family table');
    await checkboxes[1].setValue(false);
    await wrapper.get('form').trigger('submit.prevent');
    await flushPromises();

    expect(updateInvitationMock).toHaveBeenCalledWith('inv-1', {
      version: 1,
      label: 'Family table updated',
      description: 'Updated family table',
      guestIds: ['guest-1']
    });
    expect(router.currentRoute.value.name).toBe(BACKOFFICE_ROUTE_NAMES.invitationDetails);
    expect(router.currentRoute.value.params.id).toBe('inv-1');
  });

  it('shows loading state while fetching invitation data', async () => {
    getInvitationByIdMock.mockImplementation(() => new Promise(() => {}));
    listGuestsMock.mockImplementation(() => new Promise(() => {}));

    const { wrapper } = await mountView();

    expect(wrapper.text()).toContain('Loading invitation...');
  });

  it('shows error state when invitation load fails', async () => {
    getInvitationByIdMock.mockRejectedValue(new Error('Invitation not found.'));

    const { wrapper } = await mountView();

    expect(wrapper.get('[data-test="invitation-load-error"]').text()).toContain('Invitation not found.');
  });

  it('allows retry when invitation load fails', async () => {
    let callCount = 0;
    getInvitationByIdMock.mockImplementation(() => {
      callCount++;
      if (callCount === 1) {
        return Promise.reject(new Error('Invitation not found.'));
      }
      return Promise.resolve({
        id: 'inv-1',
        version: 1,
        creationDate: '2026-07-03T10:00:00Z',
        updateDate: '2026-07-03T10:00:00Z',
        label: 'Family table',
        description: 'Main family table',
        guests: [
          {
            id: 'guest-1',
            firstName: 'Alice',
            lastName: 'Martin',
            email: 'alice@example.com'
          }
        ],
        guestCount: 1
      });
    });

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
      size: 20,
      totalItems: 1,
      totalPages: 1
    });

    const { wrapper } = await mountView();

    expect(wrapper.get('[data-test="invitation-load-error"]').text()).toContain('Invitation not found.');

    await wrapper.find('button').trigger('click');
    await flushPromises();

    expect(getInvitationByIdMock).toHaveBeenCalledTimes(2);
    expect(wrapper.find('[data-test="invitation-load-error"]').exists()).toBe(false);
  });

  it('shows validation error when label is empty', async () => {
    getInvitationByIdMock.mockResolvedValue({
      id: 'inv-1',
      version: 1,
      creationDate: '2026-07-03T10:00:00Z',
      updateDate: '2026-07-03T10:00:00Z',
      label: 'Family table',
      description: 'Main family table',
      guests: [
        {
          id: 'guest-1',
          firstName: 'Alice',
          lastName: 'Martin',
          email: 'alice@example.com'
        }
      ],
      guestCount: 1
    });

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
      size: 20,
      totalItems: 1,
      totalPages: 1
    });

    const { wrapper, router } = await mountView();

    await wrapper.get('[data-test="invitation-label-input"]').setValue('   ');
    await wrapper.get('form').trigger('submit.prevent');
    await flushPromises();

    expect(wrapper.get('[data-test="invitation-validation-error"]').text()).toContain('Label is required.');
    expect(updateInvitationMock).not.toHaveBeenCalled();
    expect(router.currentRoute.value.name).toBe(BACKOFFICE_ROUTE_NAMES.invitationEdit);
  });

  it('shows validation error when no guest is selected', async () => {
    getInvitationByIdMock.mockResolvedValue({
      id: 'inv-1',
      version: 1,
      creationDate: '2026-07-03T10:00:00Z',
      updateDate: '2026-07-03T10:00:00Z',
      label: 'Family table',
      description: 'Main family table',
      guests: [
        {
          id: 'guest-1',
          firstName: 'Alice',
          lastName: 'Martin',
          email: 'alice@example.com'
        }
      ],
      guestCount: 1
    });

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
      size: 20,
      totalItems: 1,
      totalPages: 1
    });

    const { wrapper, router } = await mountView();

    const checkbox = wrapper.get('[data-test="guest-checkbox"]');
    await checkbox.setValue(false);
    await wrapper.get('form').trigger('submit.prevent');
    await flushPromises();

    expect(wrapper.get('[data-test="invitation-validation-error"]').text()).toContain('Select at least one guest.');
    expect(updateInvitationMock).not.toHaveBeenCalled();
    expect(router.currentRoute.value.name).toBe(BACKOFFICE_ROUTE_NAMES.invitationEdit);
  });

  it('shows api error when update fails', async () => {
    getInvitationByIdMock.mockResolvedValue({
      id: 'inv-1',
      version: 1,
      creationDate: '2026-07-03T10:00:00Z',
      updateDate: '2026-07-03T10:00:00Z',
      label: 'Family table',
      description: 'Main family table',
      guests: [
        {
          id: 'guest-1',
          firstName: 'Alice',
          lastName: 'Martin',
          email: 'alice@example.com'
        }
      ],
      guestCount: 1
    });

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
      size: 20,
      totalItems: 1,
      totalPages: 1
    });

    updateInvitationMock.mockRejectedValue(new Error('Invitation not found.'));

    const { wrapper, router } = await mountView();

    await wrapper.get('[data-test="invitation-label-input"]').setValue('Family table updated');
    await wrapper.get('form').trigger('submit.prevent');
    await flushPromises();

    expect(wrapper.get('[data-test="invitation-submit-error"]').text()).toContain('Invitation not found.');
    expect(router.currentRoute.value.name).toBe(BACKOFFICE_ROUTE_NAMES.invitationEdit);
  });

  it('shows conflict error when guests are already assigned concurrently', async () => {
    getInvitationByIdMock.mockResolvedValue({
      id: 'inv-1',
      version: 1,
      creationDate: '2026-07-03T10:00:00Z',
      updateDate: '2026-07-03T10:00:00Z',
      label: 'Family table',
      description: 'Main family table',
      guests: [
        {
          id: 'guest-1',
          firstName: 'Alice',
          lastName: 'Martin',
          email: 'alice@example.com'
        }
      ],
      guestCount: 1
    });

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
      size: 20,
      totalItems: 1,
      totalPages: 1
    });

    updateInvitationMock.mockRejectedValue(new Error('Some guests are already assigned to another invitation.'));

    const { wrapper, router } = await mountView();

    await wrapper.get('[data-test="invitation-label-input"]').setValue('Family table updated');
    await wrapper.get('form').trigger('submit.prevent');
    await flushPromises();

    expect(wrapper.get('[data-test="invitation-submit-error"]').text()).toContain('Some guests are already assigned to another invitation.');
    expect(router.currentRoute.value.name).toBe(BACKOFFICE_ROUTE_NAMES.invitationEdit);
  });

  it('shows empty available guests state with create guest link', async () => {
    getInvitationByIdMock.mockResolvedValue({
      id: 'inv-1',
      version: 1,
      creationDate: '2026-07-03T10:00:00Z',
      updateDate: '2026-07-03T10:00:00Z',
      label: 'Family table',
      description: 'Main family table',
      guests: [],
      guestCount: 0
    });

    listGuestsMock.mockResolvedValue({
      items: [],
      page: 0,
      size: 20,
      totalItems: 0,
      totalPages: 0
    });

    const { wrapper } = await mountView();

    expect(wrapper.get('[data-test="empty-no-guests-available"]').text()).toContain('No guests available yet.');
    expect(wrapper.get('[data-test="create-guest-link"]').attributes('href')).toBe('/guests/new');
    expect(wrapper.find('[data-test="empty-no-search-match"]').exists()).toBe(false);
  });

  it('loads guests with availability filter set to undefined for edit', async () => {
    getInvitationByIdMock.mockResolvedValue({
      id: 'inv-1',
      version: 1,
      creationDate: '2026-07-03T10:00:00Z',
      updateDate: '2026-07-03T10:00:00Z',
      label: 'Family table',
      description: 'Main family table',
      guests: [
        {
          id: 'guest-1',
          firstName: 'Alice',
          lastName: 'Martin',
          email: 'alice@example.com'
        }
      ],
      guestCount: 1
    });

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
      size: 20,
      totalItems: 1,
      totalPages: 1
    });

    await mountView();

    expect(listGuestsMock).toHaveBeenCalledWith({
      page: 0,
      size: 10,
      status: 'active',
      availability: undefined,
      search: undefined
    });
  });

  it('shows no search match message when search returns no results', async () => {
    vi.useFakeTimers();

    try {
      getInvitationByIdMock.mockResolvedValue({
        id: 'inv-1',
        version: 1,
        creationDate: '2026-07-03T10:00:00Z',
        updateDate: '2026-07-03T10:00:00Z',
        label: 'Family table',
        description: 'Main family table',
        guests: [
          {
            id: 'guest-1',
            firstName: 'Alice',
            lastName: 'Martin',
            email: 'alice@example.com'
          }
        ],
        guestCount: 1
      });

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
          size: 20,
          totalItems: 1,
          totalPages: 1
        })
        .mockResolvedValueOnce({
          items: [],
          page: 0,
          size: 20,
          totalItems: 0,
          totalPages: 0
        });

      const { wrapper } = await mountView();

      await wrapper.get('[data-test="guest-search-input"]').setValue('zzz-does-not-match');
      vi.advanceTimersByTime(301);
      await flushPromises();

      expect(listGuestsMock).toHaveBeenNthCalledWith(2, { page: 0, size: 10, status: 'active', availability: undefined, search: 'zzz-does-not-match' });
      expect(wrapper.get('[data-test="empty-no-search-match"]').text()).toContain('No guests match your search.');
      expect(wrapper.find('[data-test="empty-no-guests-available"]').exists()).toBe(false);
    } finally {
      vi.useRealTimers();
    }
  });

  it('loads more guests when scrolling near the bottom of the list', async () => {
    getInvitationByIdMock.mockResolvedValue({
      id: 'inv-1',
      version: 1,
      creationDate: '2026-07-03T10:00:00Z',
      updateDate: '2026-07-03T10:00:00Z',
      label: 'Family table',
      description: 'Main family table',
      guests: [
        {
          id: 'guest-1',
          firstName: 'Alice',
          lastName: 'Martin',
          email: 'alice@example.com'
        }
      ],
      guestCount: 1
    });

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

    expect(listGuestsMock).toHaveBeenNthCalledWith(1, { page: 0, size: 10, status: 'active', availability: undefined, search: undefined });
    expect(listGuestsMock).toHaveBeenNthCalledWith(2, { page: 1, size: 10, status: 'active', availability: undefined, search: undefined });
    expect(wrapper.text()).toContain('Alice Martin');
    expect(wrapper.text()).toContain('Zoe Durand');
  });

  it('shows error state when guest loading fails', async () => {
    getInvitationByIdMock.mockResolvedValue({
      id: 'inv-1',
      version: 1,
      creationDate: '2026-07-03T10:00:00Z',
      updateDate: '2026-07-03T10:00:00Z',
      label: 'Family table',
      description: 'Main family table',
      guests: [
        {
          id: 'guest-1',
          firstName: 'Alice',
          lastName: 'Martin',
          email: 'alice@example.com'
        }
      ],
      guestCount: 1
    });

    listGuestsMock.mockRejectedValue(new Error('Unable to retrieve guests at the moment.'));

    const { wrapper } = await mountView();

    expect(wrapper.get('[data-test="guest-load-error"]').text()).toContain('Unable to retrieve guests at the moment.');
  });

  it('trims label and description before submitting update payload', async () => {
    getInvitationByIdMock.mockResolvedValue({
      id: 'inv-1',
      version: 1,
      creationDate: '2026-07-03T10:00:00Z',
      updateDate: '2026-07-03T10:00:00Z',
      label: 'Family table',
      description: 'Main family table',
      guests: [
        {
          id: 'guest-1',
          firstName: 'Alice',
          lastName: 'Martin',
          email: 'alice@example.com'
        }
      ],
      guestCount: 1
    });

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

    updateInvitationMock.mockResolvedValue({
      id: 'inv-1',
      version: 2,
      creationDate: '2026-07-03T10:00:00Z',
      updateDate: '2026-07-04T10:00:00Z',
      label: 'Family table updated',
      description: 'Updated family table',
      guests: [
        {
          id: 'guest-1',
          firstName: 'Alice',
          lastName: 'Martin',
          email: 'alice@example.com'
        }
      ],
      guestCount: 1
    });

    const { wrapper } = await mountView();

    await wrapper.get('[data-test="invitation-label-input"]').setValue('  Family table updated  ');
    await wrapper.get('[data-test="invitation-description-input"]').setValue('  Updated family table  ');
    await wrapper.get('form').trigger('submit.prevent');
    await flushPromises();

    expect(updateInvitationMock).toHaveBeenCalledWith('inv-1', {
      version: 1,
      label: 'Family table updated',
      description: 'Updated family table',
      guestIds: ['guest-1']
    });
  });

  it('disables submit button and shows pending label while update is in progress', async () => {
    getInvitationByIdMock.mockResolvedValue({
      id: 'inv-1',
      version: 1,
      creationDate: '2026-07-03T10:00:00Z',
      updateDate: '2026-07-03T10:00:00Z',
      label: 'Family table',
      description: 'Main family table',
      guests: [
        {
          id: 'guest-1',
          firstName: 'Alice',
          lastName: 'Martin',
          email: 'alice@example.com'
        }
      ],
      guestCount: 1
    });

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

    let resolveUpdate: (() => void) | undefined;
    updateInvitationMock.mockImplementation(
      () => new Promise((resolve) => {
        resolveUpdate = () => {
          resolve({
            id: 'inv-1',
            version: 2,
            creationDate: '2026-07-03T10:00:00Z',
            updateDate: '2026-07-04T10:00:00Z',
            label: 'Family table',
            description: 'Main family table',
            guests: [],
            guestCount: 0
          });
        };
      })
    );

    const { wrapper } = await mountView();

    const submitButton = wrapper.get('[data-test="update-invitation-submit"]');

    await wrapper.get('form').trigger('submit.prevent');
    await flushPromises();

    expect(submitButton.attributes('disabled')).toBeDefined();
    expect(submitButton.text()).toContain('Updating invitation...');
    expect(updateInvitationMock).toHaveBeenCalledTimes(1);

    if (!resolveUpdate) {
      throw new Error('Expected update promise resolver to be initialized.');
    }

    resolveUpdate();
    await flushPromises();
  });
});



