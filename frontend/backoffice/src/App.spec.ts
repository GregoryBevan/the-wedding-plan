import { flushPromises, mount } from '@vue/test-utils';
import { defineComponent, h } from 'vue';
import { createMemoryHistory, createRouter } from 'vue-router';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import App from './App.vue';
import { createBackofficeRouter } from './router';
import { BACKOFFICE_ROUTE_NAMES } from './router/routeNames';
import { clearSessionAuthStatus } from './services/authStatusCache';
import { createGuestPayload } from './testFixtures/guestFixtures';

const authApiMock = vi.hoisted(() => ({
  getAuthStatus: vi.fn(),
  getGoogleLoginUrl: vi.fn(() => 'http://localhost:8080/oauth2/authorization/google'),
  logout: vi.fn()
}));

const guestApiMock = vi.hoisted(() => ({
  addGuest: vi.fn()
}));

vi.mock('./services/authApi', () => authApiMock);
vi.mock('./services/guestApi', () => guestApiMock);

const globalStubs = {
  GuestForm: true,
  GuestListView: true,
  InvitationsListView: true
};

const guestFormSubmitStub = defineComponent({
  emits: ['submit'],
  setup(_, { emit }) {
    return () => h('button', {
      'data-test': 'guest-form-submit',
      onClick: () => emit('submit', createGuestPayload())
    }, 'Submit guest form');
  }
});

describe('App auth states', () => {
  beforeEach(() => {
    clearSessionAuthStatus();
    vi.clearAllMocks();
  });

  const mountApp = async ({
    route = '/guests',
    stubs = globalStubs
  }: {
    route?: string;
    stubs?: Record<string, true | ReturnType<typeof defineComponent>>;
  } = {}) => {
    const router = createBackofficeRouter(createMemoryHistory());

    await router.push(route);
    await router.isReady();

    const wrapper = mount(App, {
      global: {
        plugins: [router],
        stubs
      }
    });

    await flushPromises();

    return { wrapper, router };
  };

  it('shows login action for unauthenticated users', async () => {
    authApiMock.getAuthStatus.mockResolvedValue({
      isAuthenticated: false,
      email: null,
      isAuthorized: false
    });

    const { wrapper } = await mountApp({ route: '/guests' });

    expect(authApiMock.getAuthStatus).toHaveBeenCalled();
    expect(wrapper.text()).toContain('Please sign in with Google to access the backoffice.');
    expect(wrapper.find('a').attributes('href')).toBe('http://localhost:8080/oauth2/authorization/google');
    expect(wrapper.get('[data-test="backoffice-version"]').text())
      .toContain(`Backoffice version ${__APP_VERSION__}`);
  });

  it('shows blocked message for unauthorized authenticated users', async () => {
    authApiMock.getAuthStatus.mockResolvedValue({
      isAuthenticated: true,
      email: 'someone@example.com',
      isAuthorized: false
    });

    const { wrapper } = await mountApp({ route: '/guests' });

    expect(wrapper.text()).toContain('Your account is not authorized to access this backoffice.');
    expect(wrapper.text()).toContain('Logout');
  });

  it('redirects root route to invitations list for authorized users', async () => {
    authApiMock.getAuthStatus.mockResolvedValue({
      isAuthenticated: true,
      email: 'allowed@example.com',
      isAuthorized: true
    });

    const { wrapper, router } = await mountApp({ route: '/' });

    expect(router.currentRoute.value.name).toBe(BACKOFFICE_ROUTE_NAMES.invitationList);
    expect(router.currentRoute.value.path).toBe('/invitations');
    expect(wrapper.findComponent({ name: 'InvitationsListView' }).exists()).toBe(true);
    expect(wrapper.findComponent({ name: 'GuestListView' }).exists()).toBe(false);

    await wrapper.get('[data-test="user-menu-toggle"]').trigger('click');

    expect(wrapper.text()).toContain('Signed in as');
    expect(wrapper.text()).toContain('allowed@example.com');
    expect(wrapper.text()).toContain('Logout');
  });

  it('renders left sidebar shell on protected routes while keeping routed content visible', async () => {
    authApiMock.getAuthStatus.mockResolvedValue({
      isAuthenticated: true,
      email: 'allowed@example.com',
      isAuthorized: true
    });

    const { wrapper } = await mountApp({ route: '/guests' });

    expect(wrapper.find('[data-test="backoffice-sidebar"]').exists()).toBe(true);
    expect(wrapper.find('nav[aria-label="Backoffice navigation"]').exists()).toBe(true);
    expect(wrapper.findComponent({ name: 'GuestListView' }).exists()).toBe(true);
  });

  it('does not render left sidebar shell on public routes', async () => {
    const { wrapper } = await mountApp({ route: '/login-required' });

    expect(wrapper.find('[data-test="backoffice-sidebar"]').exists()).toBe(false);
    expect(wrapper.text()).toContain('Please sign in with Google to access the backoffice.');
  });

  it('navigates to add guest route for authorized users', async () => {
    authApiMock.getAuthStatus.mockResolvedValue({
      isAuthenticated: true,
      email: 'allowed@example.com',
      isAuthorized: true
    });

    const { wrapper, router } = await mountApp({ route: '/guests' });

    await router.push({ name: BACKOFFICE_ROUTE_NAMES.guestAdd });
    await flushPromises();

    expect(router.currentRoute.value.path).toBe('/guests/new');
    expect(wrapper.text()).toContain('Add a New Guest');
    expect(wrapper.findComponent({ name: 'GuestForm' }).exists()).toBe(true);
    expect(wrapper.findComponent({ name: 'GuestListView' }).exists()).toBe(false);
  });

  it('does not refetch auth status in App when only the query string changes', async () => {
    authApiMock.getAuthStatus.mockResolvedValue({
      isAuthenticated: true,
      email: 'allowed@example.com',
      isAuthorized: true
    });

    const router = createRouter({
      history: createMemoryHistory(),
      routes: [
        {
          path: '/invitations',
          component: defineComponent(() => () => h('div', 'Invitation list')),
          meta: {
            requiresAuthorized: true
          }
        },
        {
          path: '/guests',
          component: defineComponent(() => () => h('div', 'Guest list')),
          meta: {
            requiresAuthorized: true
          }
        }
      ]
    });

    await router.push('/guests');
    await router.isReady();

    mount(App, {
      global: {
        plugins: [router]
      }
    });
    await flushPromises();

    const authStatusCallCountAfterMount = authApiMock.getAuthStatus.mock.calls.length;

    await router.push({ path: '/guests', query: { page: '2' } });
    await flushPromises();

    expect(authApiMock.getAuthStatus).toHaveBeenCalledTimes(authStatusCallCountAfterMount);
  });

  it('logs out and returns to login state', async () => {
    authApiMock.getAuthStatus.mockResolvedValue({
      isAuthenticated: true,
      email: 'allowed@example.com',
      isAuthorized: true
    });
    authApiMock.logout.mockResolvedValue(undefined);

    const { wrapper } = await mountApp({ route: '/guests' });

    await wrapper.get('[data-test="user-menu-toggle"]').trigger('click');
    await wrapper.get('[data-test="user-menu-logout"]').trigger('click');
    await flushPromises();

    expect(authApiMock.logout).toHaveBeenCalledTimes(1);
    expect(wrapper.text()).toContain('Please sign in with Google to access the backoffice.');
  });

  it('redirects to guest list after submit in add guest route', async () => {
    authApiMock.getAuthStatus.mockResolvedValue({
      isAuthenticated: true,
      email: 'allowed@example.com',
      isAuthorized: true
    });
    guestApiMock.addGuest.mockResolvedValue({ id: '1' });

    const { wrapper, router } = await mountApp({
      route: '/guests/new',
      stubs: {
        ...globalStubs,
        GuestForm: guestFormSubmitStub
      }
    });

    await wrapper.get('[data-test="guest-form-submit"]').trigger('click');
    await flushPromises();

    expect(router.currentRoute.value.path).toBe('/guests');
  });

  it('renders not found view for unknown routes', async () => {
    authApiMock.getAuthStatus.mockResolvedValue({
      isAuthenticated: true,
      email: 'allowed@example.com',
      isAuthorized: true
    });

    const { wrapper, router } = await mountApp({ route: '/unknown-page' });

    expect(router.currentRoute.value.name).toBe(BACKOFFICE_ROUTE_NAMES.notFound);
    expect(wrapper.text()).toContain('Page not found');
    expect(wrapper.text()).toContain('Go to guest list');
  });

  it('can return to add route after successful submit', async () => {
    authApiMock.getAuthStatus.mockResolvedValue({
      isAuthenticated: true,
      email: 'allowed@example.com',
      isAuthorized: true
    });
    guestApiMock.addGuest.mockResolvedValue({ id: '1' });

    const { wrapper, router } = await mountApp({
      route: '/guests/new',
      stubs: {
        ...globalStubs,
        GuestForm: guestFormSubmitStub
      }
    });

    await wrapper.get('[data-test="guest-form-submit"]').trigger('click');
    await flushPromises();

    expect(router.currentRoute.value.path).toBe('/guests');

    await router.push({ name: BACKOFFICE_ROUTE_NAMES.guestList });
    await flushPromises();
    expect(router.currentRoute.value.path).toBe('/guests');

    await router.push({ name: BACKOFFICE_ROUTE_NAMES.guestAdd });
    await flushPromises();

    expect(router.currentRoute.value.path).toBe('/guests/new');
  });
});