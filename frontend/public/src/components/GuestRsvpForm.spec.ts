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

const savedRsvp = (attendance: 'ATTENDING' | 'DECLINED', meal: 'MEAT' | 'FISH' | 'VEGGIE' | null = null) => ({
  id: 'rsvp-1',
  version: 1,
  creationDate: '2026-06-13T10:00:00',
  updateDate: '2026-06-13T10:00:00',
  attendance,
  meal,
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
    fetchRsvpMock.mockResolvedValue(savedRsvp('ATTENDING', 'VEGGIE'));
    const wrapper = mountForm();

    await flushPromises();

    expect((wrapper.find('input[value="ATTENDING"]').element as HTMLInputElement).checked).toBe(true);
    expect((wrapper.find('input[value="VEGGIE"]').element as HTMLInputElement).checked).toBe(true);
    expect(wrapper.find('button[type="submit"]').attributes('disabled')).toBeDefined();
  });

  it('enables submit when the choice differs from the saved answer and disables it again on return', async () => {
    fetchRsvpMock.mockResolvedValue(savedRsvp('ATTENDING', 'MEAT'));
    const wrapper = mountForm();
    await flushPromises();

    await wrapper.find('input[value="DECLINED"]').trigger('change');
    expect(wrapper.find('button[type="submit"]').attributes('disabled')).toBeUndefined();

    await wrapper.find('input[value="ATTENDING"]').trigger('change');
    expect(wrapper.find('button[type="submit"]').attributes('disabled')).toBeDefined();
  });

  it('hides the meal selector until the guest chooses to attend and hides it again when declining', async () => {
    fetchRsvpMock.mockResolvedValue(null);
    const wrapper = mountForm();
    await flushPromises();

    expect(wrapper.find('input[value="MEAT"]').exists()).toBe(false);

    await wrapper.find('input[value="ATTENDING"]').trigger('change');
    expect(wrapper.text()).toContain(t('rsvp.meal.question'));
    expect(wrapper.find('input[value="MEAT"]').exists()).toBe(true);

    await wrapper.find('input[value="DECLINED"]').trigger('change');
    expect(wrapper.find('input[value="MEAT"]').exists()).toBe(false);
  });

  it('blocks submit for an attending guest until a meal is chosen', async () => {
    fetchRsvpMock.mockResolvedValue(null);
    const wrapper = mountForm();
    await flushPromises();

    await wrapper.find('input[value="ATTENDING"]').trigger('change');
    expect(wrapper.find('button[type="submit"]').attributes('disabled')).toBeDefined();

    await wrapper.find('input[value="FISH"]').trigger('change');
    expect(wrapper.find('button[type="submit"]').attributes('disabled')).toBeUndefined();
  });

  it('submits the selected attendance and meal and shows a success state', async () => {
    fetchRsvpMock.mockResolvedValue(null);
    submitRsvpMock.mockResolvedValue(savedRsvp('ATTENDING', 'MEAT'));
    const wrapper = mountForm();
    await flushPromises();

    await wrapper.find('input[value="ATTENDING"]').trigger('change');
    await wrapper.find('input[value="MEAT"]').trigger('change');
    await wrapper.find('form').trigger('submit');
    await flushPromises();

    expect(submitRsvpMock).toHaveBeenCalledWith('ATTENDING', 'MEAT');
    expect(wrapper.text()).toContain(t('rsvp.saved'));
  });

  it('submits a declined answer without a meal', async () => {
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
    await wrapper.find('input[value="MEAT"]').trigger('change');
    await wrapper.find('form').trigger('submit');
    await wrapper.find('form').trigger('submit');

    expect(submitRsvpMock).toHaveBeenCalledTimes(1);

    resolveSubmit(savedRsvp('ATTENDING', 'MEAT'));
    await flushPromises();
  });

  it('surfaces a retriable error when the submission fails', async () => {
    fetchRsvpMock.mockResolvedValue(null);
    submitRsvpMock.mockRejectedValueOnce(new Error('boom'));
    submitRsvpMock.mockResolvedValueOnce(savedRsvp('ATTENDING', 'MEAT'));
    const wrapper = mountForm();
    await flushPromises();

    await wrapper.find('input[value="ATTENDING"]').trigger('change');
    await wrapper.find('input[value="MEAT"]').trigger('change');
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


