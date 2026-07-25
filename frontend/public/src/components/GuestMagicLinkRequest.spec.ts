import { flushPromises, mount } from '@vue/test-utils';
import { afterEach, describe, expect, it, vi } from 'vitest';
import GuestMagicLinkRequest from './GuestMagicLinkRequest.vue';
import { useGuestAccessI18n } from '../i18n/guestAccessI18n';

vi.mock('../services/guestAccessMagicLinkApi', () => ({
  requestMagicLink: vi.fn(),
}));

import { requestMagicLink } from '../services/guestAccessMagicLinkApi';

const requestMagicLinkMock = vi.mocked(requestMagicLink);
const { t } = useGuestAccessI18n();

const mountComponent = () =>
  mount(GuestMagicLinkRequest, { props: { token: 'invite-token', guestId: 'guest-1' } });

describe('GuestMagicLinkRequest', () => {
  afterEach(() => {
    vi.clearAllMocks();
  });

  it('requests a magic link for the guest and confirms the email was sent', async () => {
    requestMagicLinkMock.mockResolvedValue({ status: 'accepted' });
    const wrapper = mountComponent();

    await wrapper.find('button').trigger('click');
    await flushPromises();

    expect(requestMagicLinkMock).toHaveBeenCalledWith('invite-token', 'guest-1');
    expect(wrapper.text()).toContain(t('invitation.magicLink.sent'));
  });

  it('disables the button once the link has been sent', async () => {
    requestMagicLinkMock.mockResolvedValue({ status: 'accepted' });
    const wrapper = mountComponent();

    await wrapper.find('button').trigger('click');
    await flushPromises();

    expect(wrapper.find('button').attributes('disabled')).toBeDefined();
  });

  it('shows a recoverable message when the request is rate limited', async () => {
    requestMagicLinkMock.mockResolvedValue({ status: 'rateLimited', retryAfterSeconds: 30 });
    const wrapper = mountComponent();

    await wrapper.find('button').trigger('click');
    await flushPromises();

    expect(wrapper.text()).toContain(t('invitation.magicLink.rateLimited'));
  });

  it('shows an error message when the request fails', async () => {
    requestMagicLinkMock.mockRejectedValue(new Error('network down'));
    const wrapper = mountComponent();

    await wrapper.find('button').trigger('click');
    await flushPromises();

    expect(wrapper.text()).toContain(t('invitation.magicLink.error'));
  });
});

