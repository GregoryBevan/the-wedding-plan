import { flushPromises, mount } from '@vue/test-utils';
import { createMemoryHistory, createRouter } from 'vue-router';
import { defineComponent } from 'vue';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import EditGuestView from './EditGuestView.vue';
import GuestListView from './GuestListView.vue';
import { BACKOFFICE_ROUTE_NAMES } from '../router/routeNames';
import { createGuestResponse } from '../testFixtures/guestFixtures';

const getGuestByIdMock = vi.hoisted(() => vi.fn());
const updateGuestMock = vi.hoisted(() => vi.fn());
const openConfirmMock = vi.hoisted(() => vi.fn());
const showToastMock = vi.hoisted(() => vi.fn());

vi.mock('../services/guestApi', async (importOriginal) => {
  const module = await importOriginal<typeof import('../services/guestApi')>();

  return {
    ...module,
    getGuestById: getGuestByIdMock,
    updateGuest: updateGuestMock
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

describe('EditGuestView', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  const mountView = async (initialPath = '/guests/1/edit') => {
    const router = createRouter({
      history: createMemoryHistory(),
      routes: [
        {
          path: '/guests/:id/edit',
          name: BACKOFFICE_ROUTE_NAMES.guestEdit,
          component: EditGuestView
        },
        {
          path: '/guests',
          name: BACKOFFICE_ROUTE_NAMES.guestList,
          component: GuestListView
        },
        {
          path: '/guests/new',
          name: BACKOFFICE_ROUTE_NAMES.guestAdd,
          component: defineComponent({
            template: '<div />'
          })
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

  it('loads guest details and hydrates form fields', async () => {
    getGuestByIdMock.mockResolvedValue(createGuestResponse({
      id: '1',
      firstName: 'Jane',
      lastName: 'Doe',
      email: 'jane.doe@email.com'
    }));

    const { wrapper } = await mountView();
    const inputs = wrapper.findAll('input');

    expect(getGuestByIdMock).toHaveBeenCalledWith('1');
    expect((inputs[0].element as HTMLInputElement).value).toBe('Jane');
    expect((inputs[1].element as HTMLInputElement).value).toBe('Doe');
    expect((inputs[2].element as HTMLInputElement).value).toBe('jane.doe@email.com');
  });

  it('submits guest update, shows success toast, and navigates to guest list', async () => {
    getGuestByIdMock.mockResolvedValue(createGuestResponse({ id: '1', version: 2 }));
    updateGuestMock.mockResolvedValue(createGuestResponse({ id: '1', version: 3 }));

    const { wrapper, router } = await mountView('/guests/1/edit?page=2&size=10');

    await wrapper.get('#firstName').setValue('Updated');
    await wrapper.get('form').trigger('submit.prevent');
    await flushPromises();

    expect(updateGuestMock).toHaveBeenCalledWith('1', {
      version: 2,
      firstName: 'Updated',
      lastName: 'Doe',
      email: 'john.doe@email.com'
    });
    expect(showToastMock).toHaveBeenCalledWith('Guest updated successfully.', 'success');
    expect(router.currentRoute.value.name).toBe(BACKOFFICE_ROUTE_NAMES.guestList);
    expect(router.currentRoute.value.query.page).toBe('2');
    expect(router.currentRoute.value.query.size).toBe('10');
  });

  it('shows error toast and stays on edit view when update fails', async () => {
    getGuestByIdMock.mockResolvedValue(createGuestResponse({ id: '1', version: 2 }));
    updateGuestMock.mockRejectedValue(new Error('Unable to update guest at the moment.'));

    const { wrapper, router } = await mountView('/guests/1/edit');

    await wrapper.get('#firstName').setValue('Updated');
    await wrapper.get('form').trigger('submit.prevent');
    await flushPromises();

    expect(showToastMock).toHaveBeenCalledWith('Unable to update guest at the moment.', 'error');
    expect(router.currentRoute.value.name).toBe(BACKOFFICE_ROUTE_NAMES.guestEdit);
  });

  it('prompts before leaving when form has unsaved changes', async () => {
    getGuestByIdMock.mockResolvedValue(createGuestResponse({ id: '1' }));
    openConfirmMock.mockResolvedValue(false);

    const { wrapper, router } = await mountView();

    await wrapper.get('#firstName').setValue('Unsaved');
    const result = await router.push('/guests');
    await flushPromises();

    expect(openConfirmMock).toHaveBeenCalledWith({
      title: 'Unsaved Changes',
      message: 'You have unsaved guest details. Do you want to leave this page?',
      confirmLabel: 'Leave',
      cancelLabel: 'Continue editing'
    });
    expect(result).toBeDefined();
    expect(router.currentRoute.value.path).toBe('/guests/1/edit');
  });

  it('keeps pagination query when navigating back to guest list', async () => {
    getGuestByIdMock.mockResolvedValue(createGuestResponse({ id: '1' }));

    const { wrapper, router } = await mountView('/guests/1/edit?page=2&size=10');

    const cancelButton = wrapper.findAll('button').find((btn) => btn.text().includes('Cancel'));
    await cancelButton?.trigger('click');
    await flushPromises();

    expect(router.currentRoute.value.name).toBe(BACKOFFICE_ROUTE_NAMES.guestList);
    expect(router.currentRoute.value.query.page).toBe('2');
    expect(router.currentRoute.value.query.size).toBe('10');
  });

  it('shows load error when guest cannot be retrieved', async () => {
    getGuestByIdMock.mockRejectedValue(new Error('Guest not found.'));

    const { wrapper } = await mountView('/guests/missing/edit');

    expect(wrapper.text()).toContain('Guest not found.');
    expect(updateGuestMock).not.toHaveBeenCalled();
  });
});


