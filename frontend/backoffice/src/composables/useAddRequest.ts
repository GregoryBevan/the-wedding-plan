import { ref } from 'vue';

interface SubmitOptions {
  successMessage?: string;
  resetMessages?: boolean;
}

export function useAddRequest<TPayload, TResult>(
  addFn: (payload: TPayload) => Promise<TResult>
) {
  const isSubmitting = ref(false);
  const errorMessage = ref('');
  const successMessage = ref('');

  const clearMessages = () => {
    errorMessage.value = '';
    successMessage.value = '';
  };

  const submit = async (
    payload: TPayload,
    options?: SubmitOptions
  ): Promise<TResult | null> => {
    if (isSubmitting.value) {
      return null;
    }

    if (options?.resetMessages !== false) {
      clearMessages();
    }

    isSubmitting.value = true;

    try {
      const result = await addFn(payload);

      if (options?.successMessage) {
        successMessage.value = options.successMessage;
      }

      return result;
    } catch (error: unknown) {
      errorMessage.value = error instanceof Error
        ? error.message
        : 'Unexpected error while processing request.';
      return null;
    } finally {
      isSubmitting.value = false;
    }
  };

  return {
    isSubmitting,
    errorMessage,
    successMessage,
    clearMessages,
    submit
  };
}