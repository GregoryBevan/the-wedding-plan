import { flushPromises, mount } from '@vue/test-utils';
import { createMemoryHistory, createRouter } from 'vue-router';
import { defineComponent } from 'vue';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import InvitationDetailsView from './InvitationDetailsView.vue';
import { BACKOFFICE_ROUTE_NAMES } from '../router/routeNames';

const dateTimeFormatter = new Intl.DateTimeFormat(undefined, {
  dateStyle: 'medium',
  timeStyle: 'short'
});

const getInvitationByIdMock = vi.hoisted(() => vi.fn());

vi.mock('../services/invitationApi', () => ({
  getInvitationById: getInvitationByIdMock
}));

describe('InvitationDetailsView', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  const mountView = async ({
    initialPath = '/invitations/inv-1',
    previousPath
  }: {
    initialPath?: string;
    previousPath?: string;
  } = {}) => {
    const router = createRouter({
      history: createMemoryHistory(),
      routes: [
        {
          path: '/invitations',
          name: BACKOFFICE_ROUTE_NAMES.invitationList,
          component: defineComponent({
            template: '<div>invitations list view</div>'
          })
        },
        {
          path: '/invitations/:id/edit',
          name: BACKOFFICE_ROUTE_NAMES.invitationEdit,
          component: defineComponent({
            template: '<div>edit invitation view</div>'
          })
        },
        {
          path: '/invitations/:id',
          name: BACKOFFICE_ROUTE_NAMES.invitationDetails,
          component: InvitationDetailsView
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
    getInvitationByIdMock.mockResolvedValue({
      id: 'inv-1',
      version: 1,
      creationDate: '2026-07-03T10:00:00Z',
      updateDate: '2026-07-04T10:00:00Z',
      label: 'Family table',
      description: 'Main family table',
      guests: [],
      guestCount: 0
    });

    const { wrapper, router } = await mountView({ previousPath: '/invitations' });
    const backSpy = vi.spyOn(router, 'back');

    window.history.replaceState({ back: '/invitations' }, '');

    await wrapper.get('[data-test="back-invitation-details"]').trigger('click');
    await flushPromises();

    expect(backSpy).toHaveBeenCalled();
    expect(router.currentRoute.value.path).toBe('/invitations');

  it('renders invitation details', async () => {
    getInvitationByIdMock.mockResolvedValue({
      id: 'inv-1',
      creationDate: '2026-07-03T10:00:00Z',
      updateDate: '2026-07-04T10:00:00Z',
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

    const { wrapper } = await mountView();

    expect(getInvitationByIdMock).toHaveBeenCalledWith('inv-1');
    expect(wrapper.get('[data-test="invitation-details-card"]')).toBeDefined();
    expect(wrapper.get('[data-test="invitation-details-label"]').text()).toBe('Family table');
    expect(wrapper.get('[data-test="invitation-details-description"]').text()).toBe('Main family table');
    expect(wrapper.get('[data-test="invitation-details-guest-count"]').text()).toBe('2');
    expect(wrapper.get('[data-test="invitation-details-creation-date"]').text()).toBe(
      dateTimeFormatter.format(new Date('2026-07-03T10:00:00Z'))
    );
    expect(wrapper.get('[data-test="invitation-details-update-date"]').text()).toBe(
      dateTimeFormatter.format(new Date('2026-07-04T10:00:00Z'))
    );
    expect(wrapper.find('[data-test="invitation-details-guests-empty"]').exists()).toBe(false);
    expect(wrapper.find('[data-test="invitation-details-guests-list"]').exists()).toBe(true);
    expect(wrapper.findAll('[data-test="invitation-details-guest-item"]')).toHaveLength(2);
    expect(wrapper.text()).toContain('Alice Martin (alice@example.com)');
    expect(wrapper.text()).toContain('Bob Durand (bob@example.com)');
  });

  it('renders empty guest list state', async () => {
    getInvitationByIdMock.mockResolvedValue({
      id: 'inv-1',
      creationDate: '2026-07-03T10:00:00Z',
      updateDate: '2026-07-04T10:00:00Z',
      label: 'Family table',
      description: 'Main family table',
      guests: [],
      guestCount: 0
    });

    const { wrapper } = await mountView();

    expect(wrapper.get('[data-test="invitation-details-guests-empty"]').text()).toBe('No guests assigned to this invitation.');
    expect(wrapper.find('[data-test="invitation-details-guests-list"]').exists()).toBe(false);
  });

  it('renders error state when loading invitation fails', async () => {
    getInvitationByIdMock.mockRejectedValue(new Error('Invitation not found.'));

    const { wrapper } = await mountView({ initialPath: '/invitations/inv-404' });

    expect(wrapper.get('[data-test="invitation-details-error"]').text()).toBe('Invitation not found.');
    expect(wrapper.find('[data-test="invitation-details-card"]').exists()).toBe(false);
  });
});
