import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { useToast } from './useToast';

describe('useToast', () => {
  beforeEach(() => {
    vi.useFakeTimers();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('adds a toast message', () => {
    const { toasts, showToast } = useToast();

    showToast('Saved', 'success', 5000);

    expect(toasts.value.some((toast) => toast.message === 'Saved' && toast.type === 'success')).toBe(true);
  });

  it('auto-dismisses toast after timeout', () => {
    const { toasts, showToast } = useToast();

    showToast('Oops', 'error', 1000);
    expect(toasts.value.length).toBeGreaterThan(0);

    vi.advanceTimersByTime(1000);

    expect(toasts.value.some((toast) => toast.message === 'Oops')).toBe(false);
  });

  it('dismisses toast manually', () => {
    const { toasts, showToast, dismissToast } = useToast();

    showToast('Manual', 'success', 5000);
    const toast = toasts.value.find((item) => item.message === 'Manual');
    expect(toast).toBeDefined();

    dismissToast(toast!.id);

    expect(toasts.value.some((item) => item.id === toast!.id)).toBe(false);
  });
});


