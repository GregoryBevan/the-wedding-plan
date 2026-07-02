import { mount } from '@vue/test-utils';
import { createMemoryHistory, createRouter } from 'vue-router';
import { describe, expect, it } from 'vitest';
import BackofficeSidebar from './BackofficeSidebar.vue';

describe('BackofficeSidebar', () => {
  it('renders menu entries in the expected order with the expected routes', async () => {
    const router = createRouter({
      history: createMemoryHistory(),
      routes: [
        { path: '/invitations', component: { template: '<div />' } },
        { path: '/guests', component: { template: '<div />' } }
      ]
    });

    await router.push('/guests');
    await router.isReady();

    const wrapper = mount(BackofficeSidebar, {
      global: {
        plugins: [router]
      }
    });

    const links = wrapper.findAll('a');

    expect(links).toHaveLength(2);
    expect(links[0].text()).toBe('Invitations');
    expect(links[0].attributes('href')).toBe('/invitations');
    expect(links[1].text()).toBe('Guests');
    expect(links[1].attributes('href')).toBe('/guests');
  });
});

