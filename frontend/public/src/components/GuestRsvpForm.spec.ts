import { flushPromises, mount } from '@vue/test-utils';
import { afterEach, describe, expect, it, vi } from 'vitest';
import GuestRsvpForm from './GuestRsvpForm.vue';
import { useGuestAccessI18n } from '../i18n/guestAccessI18n';

vi.mock('../services/guestAccessSecuredApi', () => ({
  fetchRsvp: vi.fn(),
  submitRsvp: vi.fn(),
}));

import { fetchRsvp, submitRsvp } from '../services/guestAccessSecuredApi';

const fetchRsvpMock = vi.mocked(fetchRsvp);
const submitRsvpMock = vi.mocked(submitRsvp);
const { t } = useGuestAccessI18n();

const savedRsvp = (attendance: 'ATTENDING' | 'DECLINED') => ({
  id: 'rsvp-1',
  version: 1,
  creationDate: '2026-06-13T10:00:00',
  updateDate: '2026-06-13T10:00:00',
  attendance,
});

const mountForm = () => mount(GuestRsvpForm);

describe('GuestRsvpForm', () => {
  afterEach(() => {
    vi.clearAllMocks();
  });

  it('renders the empty form with a disabled submit when the guest has not responded', async () => {
    fetchRsvpMock.mockResolvedValue(null);
    const wrapper = mountForm();

    await flushPromises();

    expect(wrapper.text()).toContain(t('rsvp.question'));
    expect((wrapper.find('input[value="ATTENDING"]').element as HTMLInputElement).checked).toBe(false);
    expect(wrapper.find('button[type="submit"]').attributes('disabled')).toBeDefined();
  });

  it('prefills the form from an existing response and disables submit until the choice changes', async () => {
    fetchRsvpMock.mockResolvedValue(savedRsvp('ATTENDING'));
    const wrapper = mountForm();

    await flushPromises();

    expect((wrapper.find('input[value="ATTENDING"]').element as HTMLInputElement).checked).toBe(true);
    expect(wrapper.find('button[type="submit"]').attributes('disabled')).toBeDefined();
  });

  it('enables submit when the choice differs from the saved answer and disables it again on return', async () => {
    fetchRsvpMock.mockResolvedValue(savedRsvp('ATTENDING'));
    const wrapper = mountForm();
    await flushPromises();

    await wrapper.find('input[value="DECLINED"]').trigger('change');
    expect(wrapper.find('button[type="submit"]').attributes('disabled')).toBeUndefined();

    await wrapper.find('input[value="ATTENDING"]').trigger('change');
    expect(wrapper.find('button[type="submit"]').attributes('disabled')).toBeDefined();
  });

  it('submits the selected attendance and shows a success state', async () => {
    fetchRsvpMock.mockResolvedValue(null);
    submitRsvpMock.mockResolvedValue(savedRsvp('DECLINED'));
    const wrapper = mountForm();
    await flushPromises();

    await wrapper.find('input[value="DECLINED"]').trigger('change');
    await wrapper.find('form').trigger('submit');
    await flushPromises();

    expect(submitRsvpMock).toHaveBeenCalledWith('DECLINED');
    expect(wrapper.text()).toContain(t('rsvp.saved'));
  });

  it('does not fire a duplicate submission when triggered again while one is in flight', async () => {
    fetchRsvpMock.mockResolvedValue(null);
    let resolveSubmit: (value: ReturnType<typeof savedRsvp>) => void = () => {};
    submitRsvpMock.mockReturnValue(
      new Promise((resolve) => {
        resolveSubmit = resolve;
      }),
    );
    const wrapper = mountForm();
    await flushPromises();

    await wrapper.find('input[value="ATTENDING"]').trigger('change');
    await wrapper.find('form').trigger('submit');
    await wrapper.find('form').trigger('submit');

    expect(submitRsvpMock).toHaveBeenCalledTimes(1);

    resolveSubmit(savedRsvp('ATTENDING'));
    await flushPromises();
  });

  it('surfaces a retriable error when the submission fails', async () => {
    fetchRsvpMock.mockResolvedValue(null);
    submitRsvpMock.mockRejectedValueOnce(new Error('boom'));
    submitRsvpMock.mockResolvedValueOnce(savedRsvp('ATTENDING'));
    const wrapper = mountForm();
    await flushPromises();

    await wrapper.find('input[value="ATTENDING"]').trigger('change');
    await wrapper.find('form').trigger('submit');
    await flushPromises();

    expect(wrapper.text()).toContain(t('rsvp.submitError'));

    await wrapper.find('form').trigger('submit');
    await flushPromises();

    expect(wrapper.text()).toContain(t('rsvp.saved'));
  });

  it('shows a retriable load error when the RSVP cannot be loaded', async () => {
    fetchRsvpMock.mockRejectedValueOnce(new Error('boom'));
    fetchRsvpMock.mockResolvedValueOnce(null);
    const wrapper = mountForm();
    await flushPromises();

    expect(wrapper.text()).toContain(t('rsvp.loadError'));

    await wrapper.find('button').trigger('click');
    await flushPromises();

    expect(wrapper.text()).toContain(t('rsvp.question'));
  });
});


