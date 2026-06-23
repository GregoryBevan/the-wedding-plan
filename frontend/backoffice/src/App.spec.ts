import { flushPromises, mount } from '@vue/test-utils';
import { defineComponent, h } from 'vue';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import App from './App.vue';
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
  GuestList: true
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
    vi.clearAllMocks();
  });

  it('shows login action for unauthenticated users', async () => {
    authApiMock.getAuthStatus.mockResolvedValue({
      isAuthenticated: false,
      email: null,
      isAuthorized: false
    });

    const wrapper = mount(App, {
      global: {
        stubs: globalStubs
      }
    });

    await flushPromises();

    expect(wrapper.text()).toContain('Please sign in with Google to access the backoffice.');
    expect(wrapper.find('a').attributes('href')).toBe('http://localhost:8080/oauth2/authorization/google');
  });

  it('shows blocked message for unauthorized authenticated users', async () => {
    authApiMock.getAuthStatus.mockResolvedValue({
      isAuthenticated: true,
      email: 'someone@example.com',
      isAuthorized: false
    });

    const wrapper = mount(App, {
      global: {
        stubs: globalStubs
      }
    });

    await flushPromises();

    expect(wrapper.text()).toContain('Your account is not authorized to access this backoffice.');
    expect(wrapper.text()).toContain('Logout');
  });

  it('shows guest list view for authorized users', async () => {
    authApiMock.getAuthStatus.mockResolvedValue({
      isAuthenticated: true,
      email: 'allowed@example.com',
      isAuthorized: true
    });

    const wrapper = mount(App, {
      global: {
        stubs: globalStubs
      }
    });

    await flushPromises();

    expect(wrapper.text()).toContain('Guest list');
    expect(wrapper.findComponent({ name: 'GuestList' }).exists()).toBe(true);
    expect(wrapper.text()).toContain('Logout');
  });

  it('switches to add guest view when clicking add guest button', async () => {
    authApiMock.getAuthStatus.mockResolvedValue({
      isAuthenticated: true,
      email: 'allowed@example.com',
      isAuthorized: true
    });

    const wrapper = mount(App, {
      global: {
        stubs: globalStubs
      }
    });

    await flushPromises();

    const addGuestButton = wrapper.findAll('button').find((button) => button.text().includes('Add guest'));
    expect(addGuestButton).toBeDefined();
    await addGuestButton!.trigger('click');

    expect(wrapper.text()).toContain('Add a New Guest');
    expect(wrapper.findComponent({ name: 'GuestForm' }).exists()).toBe(true);
  });

  it('logs out and returns to login state', async () => {
    authApiMock.getAuthStatus.mockResolvedValue({
      isAuthenticated: true,
      email: 'allowed@example.com',
      isAuthorized: true
    });
    authApiMock.logout.mockResolvedValue(undefined);

    const wrapper = mount(App, {
      global: {
        stubs: globalStubs
      }
    });

    await flushPromises();
    await wrapper.get('button').trigger('click');
    await flushPromises();

    expect(authApiMock.logout).toHaveBeenCalledTimes(1);
    expect(wrapper.text()).toContain('Please sign in with Google to access the backoffice.');
  });

  it('shows add guest success message after submit in add view', async () => {
    authApiMock.getAuthStatus.mockResolvedValue({
      isAuthenticated: true,
      email: 'allowed@example.com',
      isAuthorized: true
    });
    guestApiMock.addGuest.mockResolvedValue({ id: '1' });

    const wrapper = mount(App, {
      global: {
        stubs: {
          ...globalStubs,
          GuestForm: guestFormSubmitStub
        }
      }
    });

    await flushPromises();

    const addGuestButton = wrapper.findAll('button').find((button) => button.text().includes('Add guest'));
    expect(addGuestButton).toBeDefined();
    await addGuestButton!.trigger('click');

    await wrapper.get('[data-test="guest-form-submit"]').trigger('click');
    await flushPromises();

    expect(wrapper.text()).toContain('Guest added successfully.');
  });

  it('clears add guest success message after switching away and back to add view', async () => {
    authApiMock.getAuthStatus.mockResolvedValue({
      isAuthenticated: true,
      email: 'allowed@example.com',
      isAuthorized: true
    });
    guestApiMock.addGuest.mockResolvedValue({ id: '1' });

    const wrapper = mount(App, {
      global: {
        stubs: {
          ...globalStubs,
          GuestForm: guestFormSubmitStub
        }
      }
    });

    await flushPromises();

    const addGuestButton = wrapper.findAll('button').find((button) => button.text().includes('Add guest'));
    const guestListButton = wrapper.findAll('button').find((button) => button.text().includes('Guest list'));

    expect(addGuestButton).toBeDefined();
    expect(guestListButton).toBeDefined();

    await addGuestButton!.trigger('click');
    await wrapper.get('[data-test="guest-form-submit"]').trigger('click');
    await flushPromises();

    expect(wrapper.text()).toContain('Guest added successfully.');

    await guestListButton!.trigger('click');
    await addGuestButton!.trigger('click');
    await flushPromises();

    expect(wrapper.text()).not.toContain('Guest added successfully.');
  });
});