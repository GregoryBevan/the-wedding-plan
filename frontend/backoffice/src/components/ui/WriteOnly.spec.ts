import { afterEach, describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import { h } from 'vue';
import WriteOnly from './WriteOnly.vue';
import { applyCapabilities, resetCapabilities } from '../../composables/useCapabilities';

const mountWriteOnly = () => mount(WriteOnly, {
  slots: {
    default: () => h('button', { 'data-test': 'guarded-action' }, 'Write action')
  }
});

describe('WriteOnly', () => {
  afterEach(() => {
    resetCapabilities();
  });

  it('renders its slot when the user can write', () => {
    applyCapabilities({ canWrite: true });

    const wrapper = mountWriteOnly();

    expect(wrapper.find('[data-test="guarded-action"]').exists()).toBe(true);
  });

  it('hides its slot when the user cannot write', () => {
    resetCapabilities();

    const wrapper = mountWriteOnly();

    expect(wrapper.find('[data-test="guarded-action"]').exists()).toBe(false);
  });
});

