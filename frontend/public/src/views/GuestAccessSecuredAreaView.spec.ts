import { flushPromises, mount } from '@vue/test-utils';
import { afterEach, describe, expect, it, vi } from 'vitest';
import GuestAccessSecuredAreaView from './GuestAccessSecuredAreaView.vue';
import { useGuestAccessI18n } from '../i18n/guestAccessI18n';

vi.mock('../services/guestAccessSecuredApi', () => ({
  fetchGuestSession: vi.fn(),
}));

const { routeMock } = vi.hoisted(() => ({ routeMock: { query: {} as Record<string, string> } }));

vi.mock('vue-router', async (importOriginal) => {
  const actual = await importOriginal<typeof import('vue-router')>();
  return { ...actual, useRoute: () => routeMock };
});

import { fetchGuestSession } from '../services/guestAccessSecuredApi';

const fetchGuestSessionMock = vi.mocked(fetchGuestSession);
const { t } = useGuestAccessI18n();

const mountView = () =>
  mount(GuestAccessSecuredAreaView, {
    global: {
      stubs: {
        LanguageSwitcher: true,
        RouterLink: { template: '<a><slot /></a>' },
      },
    },
  });

describe('GuestAccessSecuredAreaView', () => {
  afterEach(() => {
    vi.clearAllMocks();
    routeMock.query = {};
  });

  it('unlocks the RSVP area when the guest session is verified', async () => {
    fetchGuestSessionMock.mockResolvedValue({
      guestId: 'guest-1',
      invitationId: 'invitation-1',
      firstName: 'Alice',
      lastName: 'Martin',
      language: 'FR',
    });
    const wrapper = mountView();

    await flushPromises();

    expect(wrapper.text()).toContain(t('securedArea.verifiedIntro'));
    expect(wrapper.text()).toContain(t('securedArea.rsvpComingSoon'));
  });

  it('greets the verified guest by name', async () => {
    fetchGuestSessionMock.mockResolvedValue({
      guestId: 'guest-1',
      invitationId: 'invitation-1',
      firstName: 'Alice',
      lastName: 'Martin',
      language: 'FR',
    });
    const wrapper = mountView();

    await flushPromises();

    expect(wrapper.text()).toContain(`${t('securedArea.greeting')} Alice Martin`);
  });

  it('applies the guest language to the active locale', async () => {
    const { locale } = useGuestAccessI18n();

    fetchGuestSessionMock.mockResolvedValue({
      guestId: 'guest-1',
      invitationId: 'invitation-1',
      firstName: 'Alice',
      lastName: 'Martin',
      language: 'EN',
    });
    mountView();
    await flushPromises();
    expect(locale.value).toBe('en');

    fetchGuestSessionMock.mockResolvedValue({
      guestId: 'guest-1',
      invitationId: 'invitation-1',
      firstName: 'Alice',
      lastName: 'Martin',
      language: 'FR',
    });
    mountView();
    await flushPromises();
    expect(locale.value).toBe('fr');
  });

  it('shows a recoverable expired-link message when no session is present', async () => {
    fetchGuestSessionMock.mockResolvedValue(null);
    const wrapper = mountView();

    await flushPromises();

    expect(wrapper.text()).toContain(t('securedArea.unverifiedTitle'));
    expect(wrapper.text()).toContain(t('securedArea.unverified'));
    expect(wrapper.text()).not.toContain(t('securedArea.rsvpComingSoon'));
  });

  it('shows the recoverable expired-link state without a session lookup when redirected with linkStatus=invalid', async () => {
    routeMock.query = { linkStatus: 'invalid' };
    const wrapper = mountView();

    await flushPromises();

    expect(fetchGuestSessionMock).not.toHaveBeenCalled();
    expect(wrapper.text()).toContain(t('securedArea.unverifiedTitle'));
    expect(wrapper.text()).toContain(t('securedArea.unverified'));
  });

  it('shows a retriable error with the error title when the session lookup fails', async () => {
    fetchGuestSessionMock.mockRejectedValue(new Error('boom'));
    const wrapper = mountView();

    await flushPromises();

    expect(wrapper.text()).toContain(t('securedArea.errorTitle'));
    expect(wrapper.text()).toContain(t('securedArea.error'));
    expect(wrapper.text()).not.toContain(t('securedArea.unverifiedTitle'));
    expect(wrapper.find('button').exists()).toBe(true);
  });

  it('retries the session lookup when the retry button is clicked', async () => {
    fetchGuestSessionMock.mockRejectedValueOnce(new Error('boom'));
    fetchGuestSessionMock.mockResolvedValueOnce({
      guestId: 'guest-1',
      invitationId: 'invitation-1',
      firstName: 'Alice',
      lastName: 'Martin',
      language: 'FR',
    });
    const wrapper = mountView();
    await flushPromises();

    await wrapper.find('button').trigger('click');
    await flushPromises();

    expect(wrapper.text()).toContain(t('securedArea.verifiedIntro'));
  });
});



