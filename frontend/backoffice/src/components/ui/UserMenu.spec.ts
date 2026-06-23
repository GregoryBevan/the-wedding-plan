import { mount } from '@vue/test-utils';
import { describe, expect, it } from 'vitest';
import UserMenu from './UserMenu.vue';

describe('UserMenu', () => {
  it('opens the menu and shows user details', async () => {
    const wrapper = mount(UserMenu, {
      props: {
        userEmail: 'john.doe@email.com',
        isLoggingOut: false,
        logoutErrorMessage: ''
      }
    });

    await wrapper.get('[data-test="user-menu-toggle"]').trigger('click');

    expect(wrapper.text()).toContain('Signed in as');
    expect(wrapper.text()).toContain('john.doe@email.com');
    expect(wrapper.text()).toContain('Logout');
  });

  it('emits logout when logout button is clicked', async () => {
    const wrapper = mount(UserMenu, {
      props: {
        userEmail: 'john.doe@email.com',
        isLoggingOut: false,
        logoutErrorMessage: ''
      }
    });

    await wrapper.get('[data-test="user-menu-toggle"]').trigger('click');
    await wrapper.get('[data-test="user-menu-logout"]').trigger('click');

    expect(wrapper.emitted('logout')).toHaveLength(1);
  });

  it('closes the menu when clicking outside', async () => {
    const wrapper = mount(UserMenu, {
      props: {
        userEmail: 'john.doe@email.com',
        isLoggingOut: false,
        logoutErrorMessage: ''
      },
      attachTo: document.body
    });

    await wrapper.get('[data-test="user-menu-toggle"]').trigger('click');
    expect(wrapper.find('[data-test="user-menu-logout"]').exists()).toBe(true);

    document.body.click();
    await wrapper.vm.$nextTick();

    expect(wrapper.find('[data-test="user-menu-logout"]').exists()).toBe(false);

    wrapper.unmount();
  });
});


