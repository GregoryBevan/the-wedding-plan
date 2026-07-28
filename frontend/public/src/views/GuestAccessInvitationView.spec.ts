import { flushPromises, mount } from '@vue/test-utils';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import GuestAccessInvitationView from './GuestAccessInvitationView.vue';
import GuestMagicLinkRequest from '../components/GuestMagicLinkRequest.vue';
import { useGuestAccessI18n } from '../i18n/guestAccessI18n';

vi.mock('../services/guestAccessInvitationApi', async (importOriginal) => {
  const actual = await importOriginal<typeof import('../services/guestAccessInvitationApi')>();
  return { ...actual, resolveInvitationByToken: vi.fn() };
});

import { GuestAccessInvitationApiError, resolveInvitationByToken } from '../services/guestAccessInvitationApi';

const resolveMock = vi.mocked(resolveInvitationByToken);
const { t } = useGuestAccessI18n();

// The envelope reveal chains three timers (500 + 1000 + 500 ms) before the
// invitation content (success or error) is rendered.
const REVEAL_MS = 2000;

const invitation = {
  label: 'Famille Martin',
  description: 'Bienvenue',
  guestCount: 2,
  guests: [
    { id: 'guest-1', firstName: 'Alice', lastName: 'Martin' },
    { id: 'guest-2', firstName: 'Bob', lastName: 'Martin' },
  ],
};

const mountView = (token = 'invite-token') =>
  mount(GuestAccessInvitationView, {
    props: { token },
    global: { stubs: { LanguageSwitcher: true } },
  });

describe('GuestAccessInvitationView', () => {
  beforeEach(() => {
    vi.useFakeTimers();
  });

  afterEach(() => {
    vi.runOnlyPendingTimers();
    vi.useRealTimers();
    vi.clearAllMocks();
  });

  it('reveals the invitation and its guests for a valid token', async () => {
    resolveMock.mockResolvedValue(invitation);
    const wrapper = mountView();

    await vi.advanceTimersByTimeAsync(REVEAL_MS);

    expect(wrapper.text()).toContain('Famille Martin');
    expect(wrapper.text()).toContain('Alice');
    expect(wrapper.text()).toContain('Bob');
  });

  it('reveals the workflow instructions only when the card slide-out transition ends', async () => {
    resolveMock.mockResolvedValue(invitation);
    const wrapper = mountView();

    await vi.advanceTimersByTimeAsync(REVEAL_MS);
    expect(wrapper.text()).not.toContain(t('invitation.instructions'));

    await wrapper.find('.invitation-sheet').trigger('transitionend', { propertyName: 'transform' });
    expect(wrapper.text()).toContain(t('invitation.instructions'));
  });

  it('does not reveal the instructions on an unrelated transition', async () => {
    resolveMock.mockResolvedValue(invitation);
    const wrapper = mountView();
    await vi.advanceTimersByTimeAsync(REVEAL_MS);

    await wrapper.find('.invitation-sheet').trigger('transitionend', { propertyName: 'opacity' });

    expect(wrapper.text()).not.toContain(t('invitation.instructions'));
  });

  it('does not reveal the instructions when a child transition bubbles up to the sheet', async () => {
    resolveMock.mockResolvedValue(invitation);
    const wrapper = mountView();
    await vi.advanceTimersByTimeAsync(REVEAL_MS);

    const childTransitionEnd = new Event('transitionend', { bubbles: true });
    Object.defineProperty(childTransitionEnd, 'propertyName', { value: 'transform' });
    wrapper.find('.guest-list__item').element.dispatchEvent(childTransitionEnd);
    await flushPromises();

    expect(wrapper.text()).not.toContain(t('invitation.instructions'));
  });

  it('renders a magic-link request per guest with the matching token and guest identity', async () => {
    resolveMock.mockResolvedValue(invitation);
    const wrapper = mountView('invite-token');

    await vi.advanceTimersByTimeAsync(REVEAL_MS);

    const requests = wrapper.findAllComponents(GuestMagicLinkRequest);
    expect(requests).toHaveLength(2);
    expect(requests[0].props()).toMatchObject({
      token: 'invite-token',
      guestId: 'guest-1',
      firstName: 'Alice',
      lastName: 'Martin',
    });
  });

  it('shows a not-found message for a 404 error', async () => {
    resolveMock.mockRejectedValue(new GuestAccessInvitationApiError('nope', 404));
    const wrapper = mountView();

    await vi.advanceTimersByTimeAsync(REVEAL_MS);

    expect(wrapper.text()).toContain(t('invitation.errors.notFound'));
  });

  it('shows an invalid-link message for a 400 error', async () => {
    resolveMock.mockRejectedValue(new GuestAccessInvitationApiError('bad', 400));
    const wrapper = mountView();

    await vi.advanceTimersByTimeAsync(REVEAL_MS);

    expect(wrapper.text()).toContain(t('invitation.errors.invalidLink'));
  });

  it('shows an unavailable message for an unexpected error', async () => {
    resolveMock.mockRejectedValue(new Error('boom'));
    const wrapper = mountView();

    await vi.advanceTimersByTimeAsync(REVEAL_MS);

    expect(wrapper.text()).toContain(t('invitation.errors.unavailable'));
  });

  it('shows an invalid-link message for a blank token without calling the API', async () => {
    const wrapper = mountView('   ');

    await flushPromises();

    expect(wrapper.text()).toContain(t('invitation.errors.invalidLink'));
    expect(resolveMock).not.toHaveBeenCalled();
  });

  it('reloads the invitation when retry is clicked after an error', async () => {
    resolveMock.mockRejectedValueOnce(new GuestAccessInvitationApiError('nope', 404));
    const wrapper = mountView();
    await vi.advanceTimersByTimeAsync(REVEAL_MS);

    resolveMock.mockResolvedValueOnce(invitation);
    await wrapper.get('button').trigger('click');
    await vi.advanceTimersByTimeAsync(REVEAL_MS);

    expect(wrapper.text()).toContain('Famille Martin');
  });

  it('shows the email-sent confirmation and can return to the guest list', async () => {
    resolveMock.mockResolvedValue(invitation);
    const wrapper = mountView();
    await vi.advanceTimersByTimeAsync(REVEAL_MS);

    wrapper.findAllComponents(GuestMagicLinkRequest)[0].vm.$emit('requested', { status: 'sent', firstName: 'Alice' });
    await flushPromises();

    expect(wrapper.text()).toContain(t('invitation.magicLink.sentTitle'));
    expect(wrapper.text()).toContain(t('invitation.magicLink.sent'));
    expect(wrapper.findAllComponents(GuestMagicLinkRequest)).toHaveLength(0);

    await wrapper.get('button').trigger('click');

    expect(wrapper.findAllComponents(GuestMagicLinkRequest)).toHaveLength(2);
  });

  it('shows the rate-limited message in the confirmation without a sent title', async () => {
    resolveMock.mockResolvedValue(invitation);
    const wrapper = mountView();
    await vi.advanceTimersByTimeAsync(REVEAL_MS);

    wrapper.findAllComponents(GuestMagicLinkRequest)[0].vm.$emit('requested', { status: 'rateLimited', firstName: 'Alice' });
    await flushPromises();

    expect(wrapper.text()).toContain(t('invitation.magicLink.rateLimited'));
    expect(wrapper.text()).not.toContain(t('invitation.magicLink.sentTitle'));
  });

  it('shows the error message in the confirmation without a sent title', async () => {
    resolveMock.mockResolvedValue(invitation);
    const wrapper = mountView();
    await vi.advanceTimersByTimeAsync(REVEAL_MS);

    wrapper.findAllComponents(GuestMagicLinkRequest)[0].vm.$emit('requested', { status: 'error', firstName: 'Alice' });
    await flushPromises();

    expect(wrapper.text()).toContain(t('invitation.magicLink.error'));
    expect(wrapper.text()).not.toContain(t('invitation.magicLink.sentTitle'));
  });
});

