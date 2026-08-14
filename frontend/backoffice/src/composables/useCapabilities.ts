import { readonly, ref } from 'vue';
import type { AuthStatus } from '../services/authApi';

const canWrite = ref(false);

export const useCapabilities = () => ({
  canWrite: readonly(canWrite),
});

export const applyCapabilities = (status: Pick<AuthStatus, 'canWrite'>) => {
  canWrite.value = status.canWrite;
};

export const resetCapabilities = () => {
  canWrite.value = false;
};

