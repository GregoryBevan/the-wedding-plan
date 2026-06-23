import { flushPromises, mount } from '@vue/test-utils';
import { createMemoryHistory, createRouter } from 'vue-router';
import { defineComponent } from 'vue';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import AddGuestView from './AddGuestView.vue';
import GuestListView from './GuestListView.vue';
import { BACKOFFICE_ROUTE_NAMES } from '../router/routeNames';

const addGuestMock = vi.hoisted(() => vi.fn());

vi.mock('../services/guestApi', async (importOriginal) => {
  const module = await importOriginal<typeof import('../services/guestApi')>();

  return {
    ...module,
    addGuest: addGuestMock
  };
});

describe('AddGuestView', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  const mountView = async () => {
    const router = createRouter({
      history: createMemoryHistory(),
      routes: [
        {
          path: '/guests/new',
          name: BACKOFFICE_ROUTE_NAMES.guestAdd,
          component: AddGuestView
        },
        {
          path: '/guests',
          name: BACKOFFICE_ROUTE_NAMES.guestList,
          component: GuestListView
        }
      ]
    });

    await router.push('/guests/new');
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

  it('prompts before leaving when form has unsaved changes', async () => {
    addGuestMock.mockResolvedValue({ id: '1' });
    const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(false);

    const { wrapper, router } = await mountView();

    await wrapper.get('#firstName').setValue('John');
    const result = await router.push('/guests');
    await flushPromises();

    expect(confirmSpy).toHaveBeenCalledWith('You have unsaved guest details. Do you want to leave this page?');
    expect(result).toBeDefined();
    expect(router.currentRoute.value.path).toBe('/guests/new');

    confirmSpy.mockRestore();
  });

  it('allows leaving without confirmation when form is pristine', async () => {
    const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(false);

    const { router } = await mountView();

    await router.push('/guests');
    await flushPromises();

    expect(confirmSpy).not.toHaveBeenCalled();
    expect(router.currentRoute.value.path).toBe('/guests');

    confirmSpy.mockRestore();
  });

  it('navigates back to guest list when clicking the back icon', async () => {
    const { wrapper, router } = await mountView();

    await wrapper.get('[data-test="back-to-list"]').trigger('click');
    await flushPromises();

    expect(router.currentRoute.value.name).toBe(BACKOFFICE_ROUTE_NAMES.guestList);
  });
});


