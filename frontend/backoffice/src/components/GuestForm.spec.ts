import { mount } from '@vue/test-utils';
import { describe, expect, it } from 'vitest';
import GuestForm from './GuestForm.vue';

describe('GuestForm', () => {
  it('emits submit payload when form is valid and submitted', async () => {
    const wrapper = mount(GuestForm);

    const inputs = wrapper.findAll('input');
    await inputs[0].setValue('John');
    await inputs[1].setValue('Doe');
    await inputs[2].setValue('john.doe@email.com');

    await wrapper.find('form').trigger('submit.prevent');

    expect(wrapper.emitted('submit')).toHaveLength(1);
    expect(wrapper.emitted('submit')?.[0]).toEqual([
      {
        firstName: 'John',
        lastName: 'Doe',
        email: 'john.doe@email.com'
      }
    ]);
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