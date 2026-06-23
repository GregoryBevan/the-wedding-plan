import { mount } from '@vue/test-utils';
import { describe, expect, it } from 'vitest';
import GuestForm from './GuestForm.vue';
import { createGuestPayload } from '../testFixtures/guestFixtures';

describe('GuestForm', () => {
  it('emits submit payload when form is valid and submitted', async () => {
    const wrapper = mount(GuestForm);
    const payload = createGuestPayload();

    const inputs = wrapper.findAll('input');
    await inputs[0].setValue(payload.firstName);
    await inputs[1].setValue(payload.lastName);
    await inputs[2].setValue(payload.email);

    await wrapper.find('form').trigger('submit.prevent');

    expect(wrapper.emitted('submit')).toHaveLength(1);
    expect(wrapper.emitted('submit')?.[0]).toEqual([payload]);
  });

  it('does not emit when already submitting', async () => {
    const wrapper = mount(GuestForm, {
      props: {
        isSubmitting: true
      }
    });

    await wrapper.find('form').trigger('submit.prevent');

    expect(wrapper.emitted('submit')).toBeUndefined();
    expect(wrapper.find('button').attributes('disabled')).toBeDefined();
  });
});