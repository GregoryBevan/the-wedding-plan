import { computed, ref, unref, watch, type MaybeRef } from 'vue';
import type { GuestLanguage } from '../services/guestApi';

export interface GuestFormData {
  firstName: string;
  lastName: string;
  email: string;
  language: GuestLanguage;
}

const normalizeGuestFormData = (values?: Partial<GuestFormData>): GuestFormData => ({
  firstName: values?.firstName ?? '',
  lastName: values?.lastName ?? '',
  email: values?.email ?? '',
  language: values?.language ?? 'FR'
});

export const useGuestForm = (initialValues?: MaybeRef<Partial<GuestFormData> | undefined>) => {
  const initialState = ref(normalizeGuestFormData(unref(initialValues)));
  const form = ref<GuestFormData>({ ...initialState.value });

  watch(
    () => normalizeGuestFormData(unref(initialValues)),
    (nextInitialState) => {
      initialState.value = nextInitialState;
      form.value = { ...nextInitialState };
    }
  );

  const isDirty = computed(() => (
    form.value.firstName !== initialState.value.firstName
    || form.value.lastName !== initialState.value.lastName
    || form.value.email !== initialState.value.email
    || form.value.language !== initialState.value.language
  ));

  const setValues = (values: Partial<GuestFormData>) => {
    const normalized = normalizeGuestFormData(values);
    initialState.value = normalized;
    form.value = { ...normalized };
  };

  const reset = () => {
    form.value = { ...initialState.value };
  };

  return {
    form,
    isDirty,
    setValues,
    reset
  };
};

