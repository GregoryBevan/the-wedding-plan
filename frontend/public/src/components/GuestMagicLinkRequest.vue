<template>
  <div class="mt-2">
    <button
      class="w-full rounded-xl bg-[#093D57] px-4 py-2 text-sm font-semibold text-white disabled:cursor-not-allowed disabled:opacity-60"
      type="button"
      :disabled="isSending || status === 'sent'"
      @click="request"
    >
      {{ isSending ? t('invitation.magicLink.sending') : t('invitation.magicLink.request') }}
    </button>

    <p
      v-if="feedback"
      class="mt-2 text-xs leading-5"
      :class="status === 'error' || status === 'rateLimited' ? 'text-[#8a2b2b]' : 'text-[#093D57]/80'"
      role="status"
    >
      {{ feedback }}
    </p>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { requestMagicLink } from '../services/guestAccessMagicLinkApi';
import { useGuestAccessI18n } from '../i18n/guestAccessI18n';

const props = defineProps<{ token: string; guestId: string }>();

type RequestStatus = 'idle' | 'sending' | 'sent' | 'rateLimited' | 'error';

const status = ref<RequestStatus>('idle');
const { t } = useGuestAccessI18n();

const isSending = computed(() => status.value === 'sending');

const feedback = computed(() => {
  if (status.value === 'sent') {
    return t('invitation.magicLink.sent');
  }

  if (status.value === 'rateLimited') {
    return t('invitation.magicLink.rateLimited');
  }

  if (status.value === 'error') {
    return t('invitation.magicLink.error');
  }

  return '';
});

const request = async (): Promise<void> => {
  if (isSending.value) {
    return;
  }

  status.value = 'sending';

  try {
    const result = await requestMagicLink(props.token, props.guestId);
    status.value = result.status === 'rateLimited' ? 'rateLimited' : 'sent';
  } catch {
    status.value = 'error';
  }
};
</script>

