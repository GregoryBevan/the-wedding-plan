import { describe, expect, it, vi } from 'vitest';
import { useAddRequest } from './useAddRequest';

describe('useAddRequest', () => {
  it('submits successfully and sets success message', async () => {
    const addFn = vi.fn().mockResolvedValue({ id: 1 });
    const { submit, isSubmitting, errorMessage, successMessage } = useAddRequest(addFn);

    const result = await submit({ firstName: 'John' }, { successMessage: 'Created' });

    expect(addFn).toHaveBeenCalledWith({ firstName: 'John' });
    expect(result).toEqual({ id: 1 });
    expect(isSubmitting.value).toBe(false);
    expect(errorMessage.value).toBe('');
    expect(successMessage.value).toBe('Created');
  });

  it('handles errors and exposes error message', async () => {
    const addFn = vi.fn().mockRejectedValue(new Error('Boom'));
    const { submit, isSubmitting, errorMessage, successMessage } = useAddRequest(addFn);

    const result = await submit({ firstName: 'John' });

    expect(result).toBeNull();
    expect(isSubmitting.value).toBe(false);
    expect(errorMessage.value).toBe('Boom');
    expect(successMessage.value).toBe('');
  });

  it('prevents double submit while request is in progress', async () => {
    let resolveAdd!: (value: { id: number }) => void;
    const addFn = vi.fn(() => new Promise<{ id: number }>((resolve) => {
      resolveAdd = resolve;
    }));

    const { submit, isSubmitting } = useAddRequest(addFn);

    const firstPromise = submit({ firstName: 'John' });
    const secondResult = await submit({ firstName: 'Jane' });

    expect(isSubmitting.value).toBe(true);
    expect(addFn).toHaveBeenCalledTimes(1);
    expect(secondResult).toBeNull();

    resolveAdd({ id: 1 });
    const firstResult = await firstPromise;

    expect(firstResult).toEqual({ id: 1 });
    expect(isSubmitting.value).toBe(false);
  });
});