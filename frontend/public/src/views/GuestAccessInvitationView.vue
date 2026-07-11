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
        <p v-if="isLoading" class="text-center text-sm text-[#093D57]/80">
        <br>
        </p>

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
                v-for="(guest, index) in invitation.guests"
                :key="`${guest.firstName}-${guest.lastName}`"
                class="guest-list__item rounded-xl bg-[#BEC6C2]/30 px-4 py-3 text-[#093D57]"
                :style="{ transitionDelay: showGuestList ? `${index * 90}ms` : '0ms' }"
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
    return 'Cette invitation est introuvable. Vérifiez le lien de votre QR code.';
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
    // Wait for the flap's 1s rotation to finish (its z-index drops behind the
    // card at that point), then hold a short beat so the top of the card is
    // seen in the notch before it slides out of the opening.
    await wait(1000);
    await wait(500);
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
  bottom: 5dvh;
  transform: translateX(-50%);
  width: min(30rem, calc(100vw - 1.5rem));
  height: 50dvh;
  min-height: 20rem;
  perspective: 1200px;
}

.envelope__back {
  position: absolute;
  top: 36%;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: 0 0 14px 14px;
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
  top: 46%;
  max-height: 52%;
  overflow-y: auto;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.96);
  padding: 1.1rem;
  box-shadow: 0 16px 32px rgba(9, 61, 87, 0.18);
  /* Anchored by its top so the card's head always sits inside the front's
     notch (independent of content height): once the flap opens you see the
     top of the card in the notch, then it slides up out of the opening. */
  transform: translateY(0);
  transition: transform 1.8s cubic-bezier(0.22, 0.61, 0.36, 1);
  z-index: 3;
}

.invitation-sheet--visible {
  transform: translateY(-30dvh);
}

/* Front pocket: covers the lower part of the envelope with a V-notch top
   edge so the letter peeks out through the centre as it slides up. */
.envelope__front {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 64%;
  background: linear-gradient(165deg, #0c4663, #093d57);
  clip-path: polygon(0 0, 50% 34%, 100% 0, 100% 100%, 0 100%);
  border-radius: 0 0 14px 14px;
  box-shadow: inset 0 8px 18px rgba(9, 61, 87, 0.35);
  z-index: 5;
}

/* Flap: triangle covering the top opening, hinged at the top. When open it
   rotates upward and drops behind the invitation card (but above the back). */
.envelope__flap {
  position: absolute;
  left: 0;
  right: 0;
  top: 36%;
  height: 28%;
  background: linear-gradient(165deg, #15597d, #0b435f);
  clip-path: polygon(0 0, 100% 0, 50% 100%);
  transform-origin: top center;
  transform: rotateX(0deg);
  /* Keep the flap above the card while it is still rotating; only drop its
     z-index once the 1s rotation is finished, so the card's head never
     flashes over the flap mid-animation. */
  transition: transform 1s ease, z-index 0s linear 1s;
  /* drop-shadow follows the clipped triangle, giving a visible light rim
     so the flap reads distinctly against the front pocket. */
  filter: drop-shadow(0 0 1px rgba(231, 212, 205, 0.9)) drop-shadow(0 6px 10px rgba(9, 61, 87, 0.3));
  z-index: 6;
}

.envelope--opened .envelope__flap {
  transform: rotateX(-162deg);
  z-index: 2;
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
  /* container stays in place; each item staggers in individually */
  margin-top: 0.75rem;
}

.guest-list__item {
  transform: translateY(10px);
  opacity: 0;
  transition: transform 0.5s cubic-bezier(0.22, 0.8, 0.3, 1), opacity 0.5s ease;
}

.guest-list--revealed .guest-list__item {
  transform: translateY(0);
  opacity: 1;
}
</style>