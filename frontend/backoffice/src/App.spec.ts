import { flushPromises, mount } from '@vue/test-utils';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import App from './App.vue';

const authApiMock = vi.hoisted(() => ({
  getAuthStatus: vi.fn(),
  getGoogleLoginUrl: vi.fn(() => 'http://localhost:8080/oauth2/authorization/google'),
  logout: vi.fn()
}));

vi.mock('./services/authApi', () => authApiMock);

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
        stubs: {
          GuestForm: true
        }
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
        stubs: {
          GuestForm: true
        }
      }
    });

    await flushPromises();

    expect(wrapper.text()).toContain('Your account is not authorized to access this backoffice.');
    expect(wrapper.text()).toContain('Logout');
  });

  it('shows guest form for authorized users', async () => {
    authApiMock.getAuthStatus.mockResolvedValue({
      isAuthenticated: true,
      email: 'allowed@example.com',
      isAuthorized: true
    });

    const wrapper = mount(App, {
      global: {
        stubs: {
          GuestForm: true
        }
      }
    });

    await flushPromises();

    expect(wrapper.text()).toContain('Add a New Guest');
    expect(wrapper.findComponent({ name: 'GuestForm' }).exists()).toBe(true);
    expect(wrapper.text()).toContain('Logout');
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
        stubs: {
          GuestForm: true
        }
      }
    });

    await flushPromises();
    await wrapper.get('button').trigger('click');
    await flushPromises();

    expect(authApiMock.logout).toHaveBeenCalledTimes(1);
    expect(wrapper.text()).toContain('Please sign in with Google to access the backoffice.');
  });
});