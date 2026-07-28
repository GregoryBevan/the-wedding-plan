<template>
  <button
    class="guest-button rounded-xl bg-[#093D57] font-semibold text-white disabled:cursor-not-allowed disabled:opacity-60"
    type="button"
    :disabled="isSending"
    :aria-label="`${t('invitation.magicLink.requestFor')} ${fullName}`"
    @click="request"
  >
    {{ firstName }}
  </button>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { requestMagicLink } from '../services/guestAccessMagicLinkApi';
import { useGuestAccessI18n } from '../i18n/guestAccessI18n';

const props = defineProps<{ token: string; guestId: string; firstName: string; lastName: string }>();

export type MagicLinkRequestStatus = 'sent' | 'rateLimited' | 'error';

const emit = defineEmits<{
  requested: [{ status: MagicLinkRequestStatus; firstName: string }];
}>();

const isSending = ref(false);
const { t } = useGuestAccessI18n();

const fullName = computed(() => `${props.firstName} ${props.lastName}`);

const request = async (): Promise<void> => {
  if (isSending.value) {
    return;
  }

  isSending.value = true;

  try {
    const result = await requestMagicLink(props.token, props.guestId);
    emit('requested', {
      status: result.status === 'rateLimited' ? 'rateLimited' : 'sent',
      firstName: props.firstName,
    });
  } catch {
    emit('requested', { status: 'error', firstName: props.firstName });
  } finally {
    isSending.value = false;
  }
};
</script>

<style scoped>
/* Fluid button so guest names scale smoothly with the viewport. */
.guest-button {
  display: block;
  width: 88%;
  margin-inline: auto;
  font-size: clamp(0.82rem, 3.2vw, 0.95rem);
  padding: clamp(0.4rem, 1.5vw, 0.6rem) 0.9rem;
}

/* Keep the name buttons in scale with the smaller card text on small phones. */
@media (max-width: 420px) {
  .guest-button {
    font-size: 0.8rem;
    padding: 0.35rem 0.75rem;
  }
}
</style>

