import { ref } from 'vue';

export type ToastType = 'success' | 'error';

export interface ToastMessage {
  id: number;
  message: string;
  type: ToastType;
}

const toasts = ref<ToastMessage[]>([]);
let nextId = 1;

export const useToast = () => {
  const showToast = (message: string, type: ToastType = 'success', durationMs = 3500) => {
    const id = nextId++;
    toasts.value = [...toasts.value, { id, message, type }];

    setTimeout(() => {
      toasts.value = toasts.value.filter((toast) => toast.id !== id);
    }, durationMs);
  };

  const dismissToast = (id: number) => {
    toasts.value = toasts.value.filter((toast) => toast.id !== id);
  };

  return {
    toasts,
    showToast,
    dismissToast
  };
};

