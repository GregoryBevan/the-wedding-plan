import { flushPromises, mount } from '@vue/test-utils';
import { createMemoryHistory, createRouter } from 'vue-router';
import { defineComponent } from 'vue';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import GuestList from './GuestList.vue';
import { BACKOFFICE_ROUTE_NAMES } from '../router/routeNames';
import { createGuestPage, createGuestResponse } from '../testFixtures/guestFixtures';

const listGuestsMock = vi.hoisted(() => vi.fn());

vi.mock('../services/guestApi', () => ({
  listGuests: listGuestsMock
}));

describe('GuestList', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  const mountGuestList = async (route = '/?page=0&size=10') => {
    const router = createRouter({
      history: createMemoryHistory(),
      routes: [
        {
          path: '/',
          component: GuestList
        },
        {
          path: '/guests/new',
          name: BACKOFFICE_ROUTE_NAMES.guestAdd,
          component: defineComponent({
            template: '<div />'
          })
        },
        {
          path: '/guests/:id/edit',
          name: BACKOFFICE_ROUTE_NAMES.guestEdit,
          component: defineComponent({
            template: '<div />'
          })
        }
      ]
    });

    await router.push(route);
    await router.isReady();

    const wrapper = mount(GuestList, {
      global: {
        plugins: [router]
      }
    });

    await flushPromises();

    return { wrapper, router };
  };

  it('loads and renders guests on mount', async () => {
    listGuestsMock.mockResolvedValue(createGuestPage({
      items: [createGuestResponse()],
      totalItems: 1,
      totalPages: 1,
      size: 10
    }));

    const { wrapper } = await mountGuestList();

    expect(listGuestsMock).toHaveBeenCalledWith({ page: 0, size: 10 });
    expect(wrapper.text()).toContain('John Doe');
    expect(wrapper.text()).toContain('john.doe@email.com');
  });

  it('navigates to edit route and keeps pagination query', async () => {
    listGuestsMock.mockResolvedValue(createGuestPage({
      items: [createGuestResponse({ id: 'guest-42' })],
      totalItems: 1,
      totalPages: 1,
      size: 10,
      page: 2
    }));

    const { wrapper, router } = await mountGuestList('/?page=2&size=10');

    await wrapper.get('[data-test="edit-guest-guest-42"]').trigger('click');
    await flushPromises();

    expect(router.currentRoute.value.name).toBe(BACKOFFICE_ROUTE_NAMES.guestEdit);
    expect(router.currentRoute.value.params.id).toBe('guest-42');
    expect(router.currentRoute.value.query.page).toBe('2');
    expect(router.currentRoute.value.query.size).toBe('10');
  });

  it('requests next page when clicking next button', async () => {
    listGuestsMock
      .mockResolvedValueOnce(createGuestPage({
        items: [createGuestResponse()],
        page: 0,
        size: 10,
        totalItems: 30,
        totalPages: 2
      }))
      .mockResolvedValueOnce(createGuestPage({
        items: [createGuestResponse({
          id: '2',
          creationDate: '2026-06-23T11:00:00Z',
          updateDate: '2026-06-23T11:00:00Z',
          firstName: 'Jane',
          email: 'jane.doe@email.com'
        })],
        page: 1,
        size: 10,
        totalItems: 30,
        totalPages: 2
      }));

    const { wrapper } = await mountGuestList();

    const nextButton = wrapper.findAll('button').find((button) => button.text().includes('Next'));
    expect(nextButton).toBeDefined();
    await nextButton!.trigger('click');
    await flushPromises();

    expect(listGuestsMock).toHaveBeenNthCalledWith(2, { page: 1, size: 10 });
    expect(wrapper.text()).toContain('Jane Doe');
  });

  it('requests previous page when clicking previous button', async () => {
    listGuestsMock
      .mockResolvedValueOnce(createGuestPage({
        items: [createGuestResponse({
          id: '2',
          creationDate: '2026-06-23T11:00:00Z',
          updateDate: '2026-06-23T11:00:00Z',
          firstName: 'Jane',
          email: 'jane.doe@email.com'
        })],
        page: 1,
        size: 10,
        totalItems: 30,
        totalPages: 2
      }))
      .mockResolvedValueOnce(createGuestPage({
        items: [createGuestResponse()],
        page: 0,
        size: 10,
        totalItems: 30,
        totalPages: 2
      }));

    const { wrapper } = await mountGuestList('/?page=1&size=10');

    const previousButton = wrapper.findAll('button').find((button) => button.text().includes('Previous'));
    expect(previousButton).toBeDefined();
    await previousButton!.trigger('click');
    await flushPromises();

    expect(listGuestsMock).toHaveBeenNthCalledWith(2, { page: 0, size: 10 });
    expect(wrapper.text()).toContain('John Doe');
  });

  it('disables previous button on first page and enables it on second page', async () => {
    listGuestsMock
      .mockResolvedValueOnce(createGuestPage({
        items: [createGuestResponse()],
        page: 0,
        size: 10,
        totalItems: 30,
        totalPages: 2
      }))
      .mockResolvedValueOnce(createGuestPage({
        items: [createGuestResponse({
          id: '2',
          firstName: 'Jane',
          email: 'jane.doe@email.com'
        })],
        page: 1,
        size: 10,
        totalItems: 30,
        totalPages: 2
      }));

    const { wrapper } = await mountGuestList();

    const previousButtonOnFirstPage = wrapper.findAll('button').find((button) => button.text().includes('Previous'));
    expect(previousButtonOnFirstPage).toBeDefined();
    expect(previousButtonOnFirstPage!.attributes('disabled')).toBeDefined();

    const nextButton = wrapper.findAll('button').find((button) => button.text().includes('Next'));
    expect(nextButton).toBeDefined();
    await nextButton!.trigger('click');
    await flushPromises();

    const previousButtonOnSecondPage = wrapper.findAll('button').find((button) => button.text().includes('Previous'));
    expect(previousButtonOnSecondPage).toBeDefined();
    expect(previousButtonOnSecondPage!.attributes('disabled')).toBeUndefined();
  });

  it('shows error message and retry button when loading fails', async () => {
    listGuestsMock.mockRejectedValue(new Error('Network error'));

    const { wrapper } = await mountGuestList();

    expect(wrapper.text()).toContain('Network error');
    expect(wrapper.findAll('button').find((button) => button.text().includes('Try again'))).toBeDefined();
  });

  it('retries loading guests when clicking Try again after an error', async () => {
    listGuestsMock
      .mockRejectedValueOnce(new Error('Network error'))
      .mockResolvedValueOnce(createGuestPage({
        items: [createGuestResponse()],
        page: 0,
        size: 10,
        totalItems: 1,
        totalPages: 1
      }));

    const { wrapper } = await mountGuestList();

    expect(wrapper.text()).toContain('Network error');

    const retryButton = wrapper.findAll('button').find((button) => button.text().includes('Try again'));
    expect(retryButton).toBeDefined();

    await retryButton!.trigger('click');
    await flushPromises();

    expect(listGuestsMock).toHaveBeenCalledTimes(2);
    expect(listGuestsMock).toHaveBeenNthCalledWith(2, { page: 0, size: 10 });
    expect(wrapper.text()).not.toContain('Network error');
    expect(wrapper.text()).toContain('John Doe');
  });

  it('normalizes missing pagination query params in the URL', async () => {
    listGuestsMock.mockResolvedValue(createGuestPage({
      items: [createGuestResponse()],
      totalItems: 1,
      totalPages: 1,
      size: 10
    }));

    const { router } = await mountGuestList('/');

    expect(router.currentRoute.value.query.page).toBe('0');
    expect(router.currentRoute.value.query.size).toBe('10');
  });

  it('applies only the latest response when requests overlap', async () => {
    let resolvePage0: ((v: unknown) => void) | null = null;
    let resolvePage1: ((v: unknown) => void) | null = null;

    const page0Response = createGuestPage({
      items: [createGuestResponse({ firstName: 'Page0', email: 'p0@example.com' })],
      page: 0,
      size: 10,
      totalItems: 20,
      totalPages: 2
    });

    const page1Response = createGuestPage({
      items: [createGuestResponse({ id: '2', firstName: 'Page1', email: 'p1@example.com' })],
      page: 1,
      size: 10,
      totalItems: 20,
      totalPages: 2
    });

    listGuestsMock
      .mockImplementationOnce(() => new Promise((resolve) => { resolvePage0 = resolve; }))
      .mockImplementationOnce(() => new Promise((resolve) => { resolvePage1 = resolve; }));

    const { wrapper, router } = await mountGuestList('/?page=0&size=10');

    await router.push('/?page=1&size=10');

    resolvePage1!(page1Response);
    resolvePage0!(page0Response);

    await flushPromises();

    expect(wrapper.text()).toContain('Page1');
    expect(wrapper.text()).not.toContain('Page0');
  });
});
