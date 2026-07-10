<template>
  <main class="guest-access-page min-h-dvh">
    <section class="envelope-stage">
      <div class="envelope-stage__header">
        <p class="text-[11px] font-semibold uppercase tracking-[0.28em] text-[#A88277]">Wedding Plan</p>
        <h1 class="mt-2 text-2xl font-semibold text-[#093D57]">Votre invitation</h1>
      </div>

      <div class="envelope" :class="{ 'envelope--opened': flapOpened }">
        <div class="envelope__back" aria-hidden="true"></div>

        <article class="invitation-sheet" :class="{ 'invitation-sheet--visible': invitationVisible }">
        <p v-if="isLoading" class="text-center text-sm text-[#093D57]/80">Ouverture de votre invitation...</p>

        <div v-else-if="errorMessage" class="rounded-2xl bg-[#E7D4CD]/55 p-4 text-sm text-[#093D57]">
          <p>{{ errorMessage }}</p>
          <button
            class="mt-3 w-full rounded-xl bg-[#093D57] px-4 py-2 text-sm font-semibold text-white"
            type="button"
            @click="loadInvitation"
          >
            Reessayer
          </button>
        </div>

        <section v-else-if="invitation">
          <h2 class="text-xl font-semibold text-[#093D57]">{{ invitation.label }}</h2>
          <p class="mt-2 text-sm leading-6 text-[#093D57]/85">{{ invitation.description }}</p>

          <div class="guest-list-stage mt-4">
            <p class="text-xs font-semibold uppercase tracking-[0.2em] text-[#738F9D]">
              {{ invitation.guestCount }} invite{{ invitation.guestCount > 1 ? 's' : '' }}
            </p>

            <ul class="guest-list mt-3 space-y-2" :class="{ 'guest-list--revealed': showGuestList }">
              <li
                v-for="guest in invitation.guests"
                :key="`${guest.firstName}-${guest.lastName}`"
                class="rounded-xl bg-[#BEC6C2]/30 px-4 py-3 text-[#093D57]"
              >
                <span class="text-sm font-medium">{{ guest.firstName }} {{ guest.lastName }}</span>
              </li>
            </ul>
          </div>
        </section>
        </article>

        <div class="envelope__front" aria-hidden="true"></div>
        <div class="envelope__flap" aria-hidden="true"></div>
      </div>
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
const flapOpened = ref(false);
const invitationVisible = ref(false);

const normalizedToken = computed(() => props.token.trim());
const showGuestList = computed(() => invitationVisible.value && Boolean(invitation.value) && !isLoading.value);

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
  flapOpened.value = false;
  invitationVisible.value = false;

  if (!normalizedToken.value) {
    errorMessage.value = 'Le lien de l\'invitation est invalide.';
    isLoading.value = false;
    return;
  }

  const revealDelay = (async () => {
    await wait(500);
    flapOpened.value = true;
    // Wait for the flap's own CSS transition (1s, see .envelope__flap)
    // to fully finish before the letter starts sliding out, otherwise
    // the letter overtakes the still-opening flap.
    await wait(1000);
    invitationVisible.value = true;
  })();

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
.guest-access-page {
  background: linear-gradient(160deg, #e7d4cd 0%, #f7f4f2 38%, #bec6c2 100%);
}

.envelope-stage {
  position: relative;
  min-height: 100dvh;
  overflow: hidden;
  padding: 1.25rem 1rem 0;
}

.envelope-stage__header {
  position: relative;
  z-index: 30;
  margin: 0 auto;
  max-width: 22rem;
  text-align: center;
}

/* The envelope owns a single stacking context (via perspective), so every
   layer below stacks relative to each other. The letter lives INSIDE it,
   between the back (z1) and the front/flap, so the front can mask it. */
.envelope {
  position: absolute;
  left: 50%;
  bottom: -2dvh;
  transform: translateX(-50%);
  width: min(30rem, calc(100vw - 1.5rem));
  height: 50dvh;
  min-height: 20rem;
  perspective: 1200px;
}

.envelope__back {
  position: absolute;
  inset: 0;
  border-radius: 14px;
  background: linear-gradient(160deg, #7b97a6, #28536b);
  box-shadow: 0 20px 45px rgba(9, 61, 87, 0.28);
  z-index: 1;
}

/* Letter: sits ABOVE the back but BELOW the front, so its lower part stays
   tucked behind the pocket while its top emerges above the pocket edge. */
.invitation-sheet {
  position: absolute;
  left: 7%;
  right: 7%;
  bottom: 8%;
  max-height: 82%;
  overflow-y: auto;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.96);
  padding: 1.1rem;
  box-shadow: 0 16px 32px rgba(9, 61, 87, 0.18);
  transform: translateY(44%);
  opacity: 0;
  transition: transform 1.1s cubic-bezier(0.2, 0.86, 0.28, 1), opacity 0.5s ease;
  z-index: 2;
}

.invitation-sheet--visible {
  opacity: 1;
  transform: translateY(-78%);
}

/* Front pocket: covers the lower part of the envelope with a V-notch top
   edge so the letter peeks out through the centre as it slides up. */
.envelope__front {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 66%;
  background: linear-gradient(165deg, #0c4663, #093d57);
  clip-path: polygon(0 0, 50% 44%, 100% 0, 100% 100%, 0 100%);
  border-radius: 0 0 14px 14px;
  box-shadow: inset 0 8px 18px rgba(9, 61, 87, 0.35);
  z-index: 3;
}

/* Flap: triangle covering the top opening, hinged at the top. When open it
   simply rotates upward and stays visible above the envelope. */
.envelope__flap {
  position: absolute;
  left: 0;
  right: 0;
  top: 0;
  height: 44%;
  background: linear-gradient(165deg, #0e4a68, #093d57);
  clip-path: polygon(0 0, 100% 0, 50% 100%);
  transform-origin: top center;
  transform: rotateX(0deg);
  transition: transform 1s ease;
  box-shadow: 0 12px 24px rgba(9, 61, 87, 0.28);
  z-index: 4;
}

.envelope--opened .envelope__flap {
  transform: rotateX(-162deg);
}

.guest-list-stage {
  position: relative;
  overflow: hidden;
  padding-top: 0.2rem;
}

.guest-list-stage::before {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  top: 0;
  height: 1rem;
  background: linear-gradient(to bottom, rgba(255, 255, 255, 0.96), rgba(255, 255, 255, 0));
  z-index: 1;
}

.guest-list {
  transform: translateY(40%);
  opacity: 0;
  transition: transform 0.95s cubic-bezier(0.22, 0.8, 0.3, 1), opacity 0.5s ease;
  transition-delay: 0.35s;
}

.guest-list--revealed {
  transform: translateY(0);
  opacity: 1;
}
</style>