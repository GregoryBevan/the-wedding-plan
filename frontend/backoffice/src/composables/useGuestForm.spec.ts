import { describe, expect, it } from 'vitest';
import { ref } from 'vue';
import { useGuestForm } from './useGuestForm';

describe('useGuestForm', () => {
  it('initializes form values from provided data and resets back to initial state', () => {
    const initialValues = ref({
      firstName: 'Jane',
      lastName: 'Doe',
      email: 'jane.doe@email.com',
      language: 'FR' as const
    });

    const { form, isDirty, reset } = useGuestForm(initialValues);

    expect(form.value).toEqual(initialValues.value);
    expect(isDirty.value).toBe(false);

    form.value.firstName = 'Janet';
    expect(isDirty.value).toBe(true);

    reset();

    expect(form.value).toEqual(initialValues.value);
    expect(isDirty.value).toBe(false);
  });

  it('updates the form when initial values change', async () => {
    const initialValues = ref({
      firstName: 'John',
      lastName: 'Doe',
      email: 'john.doe@email.com',
      language: 'FR' as const
    });

    const { form, isDirty } = useGuestForm(initialValues);

    initialValues.value = {
      firstName: 'Alice',
      lastName: 'Smith',
      email: 'alice.smith@email.com',
      language: 'FR' as const
    };

    await Promise.resolve();

    expect(form.value).toEqual(initialValues.value);
    expect(isDirty.value).toBe(false);
  });

  it('sets values explicitly with setValues', () => {
    const { form, setValues, isDirty } = useGuestForm();

    setValues({
      firstName: 'Bob',
      lastName: 'Martin',
      email: 'bob.martin@email.com',
      language: 'EN'
    });

    expect(form.value).toEqual({
      firstName: 'Bob',
      lastName: 'Martin',
      email: 'bob.martin@email.com',
      language: 'EN'
    });
    expect(isDirty.value).toBe(false);
  });

  it('tracks dirty state after setValues when user modifies a field', () => {
    const { form, setValues, isDirty } = useGuestForm();

    setValues({
      firstName: 'Bob',
      lastName: 'Martin',
      email: 'bob.martin@email.com',
      language: 'FR'
    });

    expect(isDirty.value).toBe(false);

    form.value.firstName = 'Robert';
    expect(isDirty.value).toBe(true);
  });

  it('defaults language to FR and tracks dirty state when it changes', () => {
    const { form, isDirty } = useGuestForm();

    expect(form.value.language).toBe('FR');
    expect(isDirty.value).toBe(false);

    form.value.language = 'EN';
    expect(isDirty.value).toBe(true);
  });
});

