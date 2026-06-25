import { flushPromises, mount } from '@vue/test-utils';
import { createMemoryHistory, createRouter } from 'vue-router';
import { defineComponent } from 'vue';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import AddGuestView from './AddGuestView.vue';
import GuestListView from './GuestListView.vue';
import { BACKOFFICE_ROUTE_NAMES } from '../router/routeNames';

const addGuestMock = vi.hoisted(() => vi.fn());
const openConfirmMock = vi.hoisted(() => vi.fn());
const showToastMock = vi.hoisted(() => vi.fn());

vi.mock('../services/guestApi', async (importOriginal) => {
  const module = await importOriginal<typeof import('../services/guestApi')>();

  return {
    ...module,
    addGuest: addGuestMock
  };
});

vi.mock('../composables/useConfirmDialog', () => ({
  useConfirmDialog: () => ({
    openConfirm: openConfirmMock
  })
}));

vi.mock('../composables/useToast', () => ({
  useToast: () => ({
    showToast: showToastMock
  })
}));

describe('AddGuestView', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  const mountView = async (initialPath = '/guests/new') => {
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

  it('prompts before leaving when form has unsaved changes', async () => {
    addGuestMock.mockResolvedValue({ id: '1' });
    openConfirmMock.mockResolvedValue(false);

    const { wrapper, router } = await mountView();

    await wrapper.get('#firstName').setValue('John');
    const result = await router.push('/guests');
    await flushPromises();

    expect(openConfirmMock).toHaveBeenCalledWith({
      title: 'Unsaved Changes',
      message: 'You have unsaved guest details. Do you want to leave this page?',
      confirmLabel: 'Leave',
      cancelLabel: 'Continue editing'
    });
    expect(result).toBeDefined();
    expect(router.currentRoute.value.path).toBe('/guests/new');
  });

  it('allows leaving without confirmation when form is pristine', async () => {

    const { router } = await mountView();

    await router.push('/guests');
    await flushPromises();

    expect(openConfirmMock).not.toHaveBeenCalled();
    expect(router.currentRoute.value.path).toBe('/guests');
  });

  it('navigates back to guest list when clicking cancel', async () => {
    const { wrapper, router } = await mountView();

    const cancelButton = wrapper.findAll('button').find((btn) => btn.text().includes('Cancel'));
    await cancelButton?.trigger('click');
    await flushPromises();

    expect(router.currentRoute.value.name).toBe(BACKOFFICE_ROUTE_NAMES.guestList);
  });

  it('keeps pagination query when navigating back to guest list', async () => {
    const { wrapper, router } = await mountView('/guests/new?page=2&size=10');

    const cancelButton = wrapper.findAll('button').find((btn) => btn.text().includes('Cancel'));
    await cancelButton?.trigger('click');
    await flushPromises();

    expect(router.currentRoute.value.name).toBe(BACKOFFICE_ROUTE_NAMES.guestList);
    expect(router.currentRoute.value.query.page).toBe('2');
    expect(router.currentRoute.value.query.size).toBe('10');
  });

  it('shows success toast and navigates to guest list after submit', async () => {
    addGuestMock.mockResolvedValue({ id: '1' });
    const { wrapper, router } = await mountView('/guests/new?page=2&size=10');

    await wrapper.get('#firstName').setValue('John');
    await wrapper.get('#lastName').setValue('Doe');
    await wrapper.get('#email').setValue('john.doe@email.com');
    await wrapper.get('form').trigger('submit.prevent');
    await flushPromises();

    expect(showToastMock).toHaveBeenCalledWith('Guest added successfully.', 'success');
    expect(router.currentRoute.value.name).toBe(BACKOFFICE_ROUTE_NAMES.guestList);
    expect(router.currentRoute.value.query.page).toBe('2');
    expect(router.currentRoute.value.query.size).toBe('10');
  });

  it('shows error toast and stays on add view when submit fails', async () => {
    addGuestMock.mockRejectedValue(new Error('Unable to create guest at the moment.'));
    const { wrapper, router } = await mountView();

    await wrapper.get('#firstName').setValue('John');
    await wrapper.get('#lastName').setValue('Doe');
    await wrapper.get('#email').setValue('john.doe@email.com');
    await wrapper.get('form').trigger('submit.prevent');
    await flushPromises();

    expect(showToastMock).toHaveBeenCalledWith('Unable to create guest at the moment.', 'error');
    expect(router.currentRoute.value.name).toBe(BACKOFFICE_ROUTE_NAMES.guestAdd);
  });
});


