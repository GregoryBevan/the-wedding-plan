<template>
  <main class="guest-access-page mx-auto flex min-h-dvh w-full max-w-md items-center px-4 py-6">
    <section class="w-full rounded-3xl bg-white/90 p-5 shadow-2xl ring-1 ring-[#093D57]/20">
      <p class="text-[11px] font-semibold uppercase tracking-[0.28em] text-[#A88277]">Wedding Plan</p>
      <h1 class="mt-2 text-2xl font-semibold text-[#093D57]">Votre invitation</h1>

      <div class="mt-6 flex justify-center">
        <div class="envelope" :class="{ 'envelope--opened': envelopeOpened }" aria-hidden="true">
          <div class="envelope__body"></div>
          <div class="envelope__letter"></div>
          <div class="envelope__flap"></div>
        </div>
      </div>

      <p v-if="isLoading" class="mt-4 text-center text-sm text-[#093D57]/80">Ouverture de votre invitation...</p>

      <div v-else-if="errorMessage" class="mt-5 rounded-xl bg-[#E7D4CD]/55 p-4 text-sm text-[#093D57]">
        <p>{{ errorMessage }}</p>
        <button
          class="mt-3 w-full rounded-xl bg-[#093D57] px-4 py-2 text-sm font-semibold text-white"
          type="button"
          @click="loadInvitation"
        >
          Reessayer
        </button>
      </div>

      <section v-else-if="invitation" class="mt-5">
        <h2 class="text-xl font-semibold text-[#093D57]">{{ invitation.label }}</h2>
        <p class="mt-2 text-sm leading-6 text-[#093D57]/85">{{ invitation.description }}</p>

        <p class="mt-4 text-xs font-semibold uppercase tracking-[0.2em] text-[#738F9D]">
          {{ invitation.guestCount }} invite{{ invitation.guestCount > 1 ? 's' : '' }}
        </p>

        <ul class="mt-3 space-y-2">
          <li
            v-for="guest in invitation.guests"
            :key="`${guest.firstName}-${guest.lastName}`"
            class="rounded-xl bg-[#BEC6C2]/30 px-4 py-3 text-[#093D57]"
          >
            <span class="text-sm font-medium">{{ guest.firstName }} {{ guest.lastName }}</span>
          </li>
        </ul>
      </section>
    </section>
  </main>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import {
  GuestAccessInvitationApiError,
  type GuestInvitationResponse,
  resolveInvitationByToken,
} from '../services/guestAccessInvitationApi';

const props = defineProps<{ token: string }>();

const invitation = ref<GuestInvitationResponse | null>(null);
const isLoading = ref(false);
const errorMessage = ref('');
const envelopeOpened = ref(false);

const normalizedToken = computed(() => props.token.trim());

const wait = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms));

const buildErrorMessage = (status: number): string => {
  if (status === 404) {
    return 'Cette invitation est introuvable. Verifiez le lien de votre QR code.';
  }

  if (status === 400) {
    return 'Le lien de l\'invitation est invalide.';
  }

  return 'Impossible de charger cette invitation pour le moment.';
};

const loadInvitation = async (): Promise<void> => {
  invitation.value = null;
  errorMessage.value = '';
  isLoading.value = true;
  envelopeOpened.value = false;

  if (!normalizedToken.value) {
    errorMessage.value = 'Le lien de l\'invitation est invalide.';
    isLoading.value = false;
    return;
  }

  const revealDelay = wait(1300).then(() => {
    envelopeOpened.value = true;
  });

  try {
    const [resolvedInvitation] = await Promise.all([
      resolveInvitationByToken(normalizedToken.value),
      revealDelay,
    ]);

    invitation.value = resolvedInvitation;
  } catch (error) {
    await revealDelay;

    if (error instanceof GuestAccessInvitationApiError) {
      errorMessage.value = buildErrorMessage(error.status);
      return;
    }

    errorMessage.value = 'Impossible de charger cette invitation pour le moment.';
  } finally {
    isLoading.value = false;
  }
};

watch(normalizedToken, () => {
  void loadInvitation();
}, { immediate: true });
</script>

<style scoped>
.envelope {
  position: relative;
  width: 240px;
  height: 154px;
  perspective: 600px;
}

.envelope__body {
  position: absolute;
  inset: 26px 0 0;
  border-radius: 0 0 14px 14px;
  background: linear-gradient(160deg, #738f9d, #093d57);
  box-shadow: 0 15px 35px rgba(9, 61, 87, 0.25);
}

.envelope__letter {
  position: absolute;
  left: 14px;
  right: 14px;
  top: 56px;
  height: 96px;
  border-radius: 10px;
  background: linear-gradient(170deg, #ffffff, #e7d4cd);
  transition: transform 0.8s ease, top 0.8s ease;
}

.envelope__flap {
  position: absolute;
  left: 0;
  right: 0;
  top: 26px;
  height: 82px;
  clip-path: polygon(0 0, 50% 88%, 100% 0);
  transform-origin: top;
  background: #093d57;
  transition: transform 0.8s ease;
}

.envelope--opened .envelope__flap {
  transform: rotateX(180deg);
}

.envelope--opened .envelope__letter {
  top: 24px;
  transform: scale(1.03);
}
</style>

