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

  it('hydrates input fields from initial values', async () => {
    const initialValues = createGuestPayload({
      firstName: 'Alice',
      lastName: 'Martin',
      email: 'alice.martin@email.com'
    });

    const wrapper = mount(GuestForm, {
      props: {
        initialValues
      }
    });

    const inputs = wrapper.findAll('input');

    expect((inputs[0].element as HTMLInputElement).value).toBe('Alice');
    expect((inputs[1].element as HTMLInputElement).value).toBe('Martin');
    expect((inputs[2].element as HTMLInputElement).value).toBe('alice.martin@email.com');
  });

  it('keeps fields unchanged after submit when resetOnSubmit is false', async () => {
    const wrapper = mount(GuestForm, {
      props: {
        resetOnSubmit: false,
        submitLabel: 'Update Guest',
        submittingLabel: 'Updating guest...'
      }
    });

    const payload = createGuestPayload({ firstName: 'Eve' });
    const inputs = wrapper.findAll('input');
    await inputs[0].setValue(payload.firstName);
    await inputs[1].setValue(payload.lastName);
    await inputs[2].setValue(payload.email);

    await wrapper.find('form').trigger('submit.prevent');

    expect(wrapper.emitted('submit')?.[0]).toEqual([payload]);
    expect((inputs[0].element as HTMLInputElement).value).toBe(payload.firstName);
  });

  it('renders cancel and submit buttons when showCancelButton is true', async () => {
    const wrapper = mount(GuestForm, {
      props: {
        showCancelButton: true
      }
    });

    const buttons = wrapper.findAll('button');
    expect(buttons).toHaveLength(2);
    expect(buttons[0].text()).toBe('Cancel');
    expect(buttons[1].text()).toBe('Add Guest');
  });

  it('emits cancel event when cancel button is clicked', async () => {
    const wrapper = mount(GuestForm, {
      props: {
        showCancelButton: true
      }
    });

    const cancelButton = wrapper.findAll('button')[0];
    await cancelButton.trigger('click');

    expect(wrapper.emitted('cancel')).toHaveLength(1);
  });
});