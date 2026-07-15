import { flushPromises, mount } from '@vue/test-utils';
import { createMemoryHistory, createRouter } from 'vue-router';
import { defineComponent } from 'vue';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import InvitationsListView from './InvitationsListView.vue';
import { BACKOFFICE_ROUTE_NAMES } from '../router/routeNames';

const listInvitationsMock = vi.hoisted(() => vi.fn());
const listGuestsMock = vi.hoisted(() => vi.fn());

vi.mock('../services/invitationApi', () => ({
  listInvitations: listInvitationsMock
}));

vi.mock('../services/guestApi', async (importOriginal) => {
  const module = await importOriginal<typeof import('../services/guestApi')>();

  return {
    ...module,
    listGuests: listGuestsMock
  };
});

describe('InvitationsListView', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  const mountView = async () => {
    const router = createRouter({
      history: createMemoryHistory(),
      routes: [
        {
          path: '/invitations',
          name: BACKOFFICE_ROUTE_NAMES.invitationList,
          component: InvitationsListView
        },
        {
          path: '/invitations/new',
          name: BACKOFFICE_ROUTE_NAMES.invitationAdd,
          component: defineComponent({
            template: '<div>create invitation view</div>'
          })
        },
        {
          path: '/invitations/:id',
          name: BACKOFFICE_ROUTE_NAMES.invitationDetails,
          component: defineComponent({
            template: '<div>invitation details view</div>'
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
          path: '/guests/new',
          name: BACKOFFICE_ROUTE_NAMES.guestAdd,
          component: defineComponent({
            template: '<div>add guest view</div>'
          })
        }
      ]
    });

    await router.push('/invitations');
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

  it('renders invitation cards with title, guest count, creation date and actions', async () => {
    listInvitationsMock.mockResolvedValue({
      items: [
        {
          id: 'inv-1',
          version: 1,
          creationDate: '2026-07-01T10:45:28Z',
          updateDate: '2026-07-01T10:45:28Z',
          label: 'Family table',
          description: 'ignored by card content',
          guests: [
            {
              id: 'g-1',
              firstName: 'Alice',
              lastName: 'Martin',
              email: 'alice@example.com'
            },
            {
              id: 'g-2',
              firstName: 'Bob',
              lastName: 'Durand',
              email: 'bob@example.com'
            }
          ],
          guestCount: 2
        }
      ],
      page: 0,
      size: 20,
      totalItems: 1,
      totalPages: 1
    });
    const { wrapper } = await mountView();

    const cards = wrapper.findAll('[data-test="invitation-card"]');
    const createInvitationCta = wrapper.get('[data-test="create-invitation-cta"]');

    expect(cards).toHaveLength(1);
    expect(createInvitationCta.attributes('href')).toBe('/invitations/new');
    expect(wrapper.find('[data-test="invitation-card-list"]').exists()).toBe(true);
    expect(wrapper.find('table').exists()).toBe(false);
    expect(wrapper.text()).toContain('Family table');
    expect(wrapper.text()).toContain('2 guests');
    expect(wrapper.text()).toContain('Alice Martin');
    expect(wrapper.text()).toContain('Bob Durand');
    expect(wrapper.text()).not.toContain('alice@example.com');
    expect(wrapper.text()).not.toContain('bob@example.com');
    expect(cards[0].findAll('[data-test="invitation-card-guest-item"]')).toHaveLength(2);
    const links = cards[0].findAll('a');
    expect(links).toHaveLength(2);
    expect(links[0].attributes('href')).toBe('/invitations/inv-1'); // View link
    expect(links[1].attributes('href')).toBe('/invitations/inv-1/edit'); // Edit link
    expect(listGuestsMock).not.toHaveBeenCalled();
  });

  it('shows create-first-guest CTA when there are no guests', async () => {
    listInvitationsMock.mockResolvedValue({
      items: [],
      page: 0,
      size: 20,
      totalItems: 0,
      totalPages: 0
    });
    listGuestsMock.mockResolvedValue({
      items: [],
      page: 0,
      size: 1,
      totalItems: 0,
      totalPages: 0
    });

    const { wrapper } = await mountView();

    expect(wrapper.find('[data-test="empty-no-guests"]').exists()).toBe(true);
    expect(wrapper.get('[data-test="create-first-guest-cta"]').attributes('href')).toBe('/guests/new');
    expect(wrapper.text()).toContain('Create your first guest');
  });

  it('shows invitations empty state when guests exist but invitations are empty', async () => {
    listInvitationsMock.mockResolvedValue({
      items: [],
      page: 0,
      size: 20,
      totalItems: 0,
      totalPages: 0
    });
    listGuestsMock.mockResolvedValue({
      items: [],
      page: 0,
      size: 1,
      totalItems: 2,
      totalPages: 2
    });

    const { wrapper } = await mountView();

    expect(wrapper.find('[data-test="empty-no-invitations"]').exists()).toBe(true);
    expect(wrapper.find('[data-test="create-first-guest-cta"]').exists()).toBe(false);
    expect(wrapper.text()).toContain('No invitations yet.');
  });
});

