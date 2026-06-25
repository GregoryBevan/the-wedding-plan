import { ref, Ref } from 'vue';

export interface ConfirmDialogConfig {
  title: string;
  message: string;
  confirmLabel?: string;
  cancelLabel?: string;
  isDangerous?: boolean;
}

export interface ConfirmDialogState extends ConfirmDialogConfig {
  isOpen: boolean;
}

let globalDialogState: Ref<ConfirmDialogState | null> = ref(null);
let resolvePromise: ((value: boolean) => void) | null = null;

export const useConfirmDialog = () => {
  const openConfirm = async (config: ConfirmDialogConfig): Promise<boolean> => {
    globalDialogState.value = {
      isOpen: true,
      confirmLabel: 'Confirm',
      cancelLabel: 'Cancel',
      isDangerous: false,
      ...config
    };

    return new Promise((resolve) => {
      resolvePromise = (value: boolean) => {
        globalDialogState.value = null;
        resolvePromise = null;
        resolve(value);
      };
    });
  };

  const confirm = () => {
    resolvePromise?.(true);
  };

  const cancel = () => {
    resolvePromise?.(false);
  };

  return {
    openConfirm,
    confirm,
    cancel,
    dialogState: globalDialogState
  };
};


