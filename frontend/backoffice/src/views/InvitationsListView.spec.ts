import { mount } from '@vue/test-utils';
import { describe, expect, it } from 'vitest';
import InvitationsListView from './InvitationsListView.vue';

describe('InvitationsListView', () => {
  it('renders invitation cards and avoids table layout', () => {
    const wrapper = mount(InvitationsListView);

    const cards = wrapper.findAll('[data-test="invitation-card"]');

    expect(cards).toHaveLength(2);
    expect(wrapper.find('[data-test="invitation-card-list"]').exists()).toBe(true);
    expect(wrapper.find('table').exists()).toBe(false);
    expect(wrapper.text()).toContain('Family table');
    expect(wrapper.text()).toContain('Friends brunch');
  });
});

