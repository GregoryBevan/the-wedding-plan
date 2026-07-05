import { mount } from '@vue/test-utils';
import { createMemoryHistory, createRouter } from 'vue-router';
import { describe, expect, it } from 'vitest';
import BackofficeSidebar from './BackofficeSidebar.vue';

const createSidebarWrapper = async (path: string) => {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/invitations', component: { template: '<div />' } },
      { path: '/invitations/new', component: { template: '<div />' } },
      { path: '/guests', component: { template: '<div />' } },
      { path: '/guests/new', component: { template: '<div />' } }
    ]
  });

  await router.push(path);
  await router.isReady();

  return mount(BackofficeSidebar, {
    global: {
      plugins: [router]
    }
  });
};

describe('BackofficeSidebar', () => {
  it('renders menu entries in the expected order with the expected routes', async () => {
    const wrapper = await createSidebarWrapper('/guests');

    const links = wrapper.findAll('a');
    const icons = wrapper.findAll('.sidebar-link-icon svg');

    expect(links).toHaveLength(2);
    expect(icons).toHaveLength(2);
    expect(links[0].text()).toBe('Invitations');
    expect(links[0].attributes('href')).toBe('/invitations');
    expect(links[1].text()).toBe('Guests');
    expect(links[1].attributes('href')).toBe('/guests');
  });

  it('marks invitations link as active on invitations routes', async () => {
    const wrapper = await createSidebarWrapper('/invitations');

    const links = wrapper.findAll('a');

    expect(links[0].classes()).toContain('bg-primary');
    expect(links[0].classes()).toContain('text-white');
    expect(links[0].attributes('aria-current')).toBe('page');
    expect(links[1].classes()).not.toContain('bg-primary');
    expect(links[1].attributes('aria-current')).toBeUndefined();
  });

  it('marks guests link as active on guests routes', async () => {
    const wrapper = await createSidebarWrapper('/guests');

    const links = wrapper.findAll('a');

    expect(links[1].classes()).toContain('bg-primary');
    expect(links[1].classes()).toContain('text-white');
    expect(links[1].attributes('aria-current')).toBe('page');
    expect(links[0].classes()).not.toContain('bg-primary');
    expect(links[0].attributes('aria-current')).toBeUndefined();
  });

  it('marks invitations link as active on nested invitations routes', async () => {
    const wrapper = await createSidebarWrapper('/invitations/new');

    const links = wrapper.findAll('a');

    expect(links[0].classes()).toContain('bg-primary');
    expect(links[0].classes()).toContain('text-white');
    expect(links[0].attributes('aria-current')).toBe('page');
    expect(links[1].classes()).not.toContain('bg-primary');
    expect(links[1].attributes('aria-current')).toBeUndefined();
  });

  it('marks guests link as active on nested guests routes', async () => {
    const wrapper = await createSidebarWrapper('/guests/new');

    const links = wrapper.findAll('a');

    expect(links[1].classes()).toContain('bg-primary');
    expect(links[1].classes()).toContain('text-white');
    expect(links[1].attributes('aria-current')).toBe('page');
    expect(links[0].classes()).not.toContain('bg-primary');
    expect(links[0].attributes('aria-current')).toBeUndefined();
  });
});

