import { computed, ref, unref, watch, type MaybeRef } from 'vue';

export interface GuestFormData {
  firstName: string;
  lastName: string;
  email: string;
}

const normalizeGuestFormData = (values?: Partial<GuestFormData>): GuestFormData => ({
  firstName: values?.firstName ?? '',
  lastName: values?.lastName ?? '',
  email: values?.email ?? ''
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

