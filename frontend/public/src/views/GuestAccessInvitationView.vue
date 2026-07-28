<template>
  <main class="guest-access-page min-h-dvh">
    <section class="envelope-stage">
      <div class="envelope-stage__header">
        <div class="flex items-center justify-between gap-2">
          <p class="text-[11px] font-semibold uppercase tracking-[0.28em] text-[#A88277]">{{ t('common.appName') }}</p>
          <LanguageSwitcher />
        </div>
        <h1 class="mt-2 page-title font-semibold text-[#093D57]">{{ t('invitation.title') }}</h1>

        <Transition name="fade">
          <div v-if="instructionsVisible" class="mt-[3.75rem]">
            <h2 class="intro-title font-semibold text-[#093D57]">{{ t('invitation.chooseName') }}</h2>
            <p class="mx-auto mt-2 max-w-xs intro-text italic text-[#093D57]/75">{{ t('invitation.instructions') }}</p>
          </div>
        </Transition>
      </div>

      <div class="envelope" :class="{ 'envelope--opened': flapOpened }">
        <div class="envelope__back" aria-hidden="true"></div>

        <article class="invitation-sheet" :class="{ 'invitation-sheet--visible': invitationVisible }" @transitionend="onSheetTransitionEnd">
        <div class="invitation-sheet__scroll">
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
            {{ t('common.retry') }}
          </button>
        </div>

        <section v-else-if="invitation">
          <div v-if="magicLinkResult" class="magic-link-confirmation text-center">
            <p v-if="magicLinkResult.status === 'sent'" class="confirmation-title font-semibold text-[#093D57]">
              {{ t('invitation.magicLink.sentTitle') }}
            </p>
            <p class="confirmation-message mt-2 text-[#093D57]/85">{{ messageForStatus(magicLinkResult.status) }}</p>
            <button
              class="confirmation-back mt-5 rounded-xl bg-[#093D57] font-semibold text-white"
              type="button"
              @click="magicLinkResult = null"
            >
              {{ t('invitation.magicLink.back') }}
            </button>
          </div>

          <template v-else>
            <h2 class="card-label font-semibold text-[#093D57]">{{ invitation.label }}</h2>
            <p class="mt-2 card-text text-[#093D57]/85">{{ invitation.description }}</p>

            <div class="guest-list-stage mt-4">
              <p class="guest-count text-xs font-semibold uppercase tracking-[0.2em] text-[#738F9D]">
                {{ guestCountLabel(invitation.guestCount) }}
              </p>

              <ul class="guest-list mt-3 space-y-2" :class="{ 'guest-list--revealed': showGuestList }">
                <li
                  v-for="(guest, index) in invitation.guests"
                  :key="guest.id"
                  class="guest-list__item"
                  :style="{ transitionDelay: showGuestList ? `${index * 90}ms` : '0ms' }"
                >
                  <GuestMagicLinkRequest
                    :token="normalizedToken"
                    :guest-id="guest.id"
                    :first-name="guest.firstName"
                    :last-name="guest.lastName"
                    @requested="onMagicLinkRequested"
                  />
                </li>
              </ul>
            </div>
          </template>
        </section>
        </div>
        </article>

        <div class="envelope__front" aria-hidden="true"></div>
        <div class="envelope__flap" aria-hidden="true"></div>
      </div>
    </section>
  </main>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import LanguageSwitcher from '../components/LanguageSwitcher.vue';
import GuestMagicLinkRequest, { type MagicLinkRequestStatus } from '../components/GuestMagicLinkRequest.vue';
import {
  GuestAccessInvitationApiError,
  type GuestInvitationResponse,
  resolveInvitationByToken,
} from '../services/guestAccessInvitationApi';
import { useGuestAccessI18n } from '../i18n/guestAccessI18n';

const props = defineProps<{ token: string }>();

type InvitationErrorKey = 'notFound' | 'invalidLink' | 'unavailable';

const invitation = ref<GuestInvitationResponse | null>(null);
const isLoading = ref(false);
const errorKey = ref<InvitationErrorKey | null>(null);
const flapOpened = ref(false);
const invitationVisible = ref(false);
const cardRevealed = ref(false);
const magicLinkResult = ref<{ status: MagicLinkRequestStatus; firstName: string } | null>(null);

const { t, guestCountLabel } = useGuestAccessI18n();

const normalizedToken = computed(() => props.token.trim());
const showGuestList = computed(() => invitationVisible.value && Boolean(invitation.value) && !isLoading.value);
// The explanatory intro is shown only once the card's slide-out transition has ended.
const instructionsVisible = computed(() => cardRevealed.value && Boolean(invitation.value) && !isLoading.value);

const messageForStatus = (status: MagicLinkRequestStatus): string => {
  switch (status) {
    case 'sent':
      return t('invitation.magicLink.sent');
    case 'rateLimited':
      return t('invitation.magicLink.rateLimited');
    case 'error':
      return t('invitation.magicLink.error');
  }
};

const onMagicLinkRequested = (result: { status: MagicLinkRequestStatus; firstName: string }): void => {
  magicLinkResult.value = result;
};

const onSheetTransitionEnd = (event: TransitionEvent): void => {
  // `transitionend` bubbles, so ignore events re-emitted by child elements
  // (e.g. the guest list items also transition `transform`); only react to the
  // sheet's own slide-out transition.
  if (event.target === event.currentTarget && event.propertyName === 'transform') {
    cardRevealed.value = true;
  }
};
const errorMessageForKey = (key: InvitationErrorKey): string => {
  if (key === 'notFound') {
    return t('invitation.errors.notFound');
  }

  if (key === 'invalidLink') {
    return t('invitation.errors.invalidLink');
  }

  return t('invitation.errors.unavailable');
};
const errorMessage = computed(() => (errorKey.value ? errorMessageForKey(errorKey.value) : ''));

const wait = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms));

const resolveErrorKey = (status: number): InvitationErrorKey => {
  if (status === 404) {
    return 'notFound';
  }

  if (status === 400) {
    return 'invalidLink';
  }

  return 'unavailable';
};

const loadInvitation = async (): Promise<void> => {
  invitation.value = null;
  errorKey.value = null;
  isLoading.value = true;
  flapOpened.value = false;
  invitationVisible.value = false;
  cardRevealed.value = false;
  magicLinkResult.value = null;

  if (!normalizedToken.value) {
    errorKey.value = 'invalidLink';
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
      errorKey.value = resolveErrorKey(error.status);
      return;
    }

    errorKey.value = 'unavailable';
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

/* Fluid typography so the content scales smoothly across screen sizes
   (mobile-first) rather than jumping at fixed breakpoints. */
.page-title {
  font-size: clamp(1.35rem, 5.5vw, 1.9rem);
  line-height: 1.2;
}

.intro-title {
  font-size: clamp(1rem, 4vw, 1.2rem);
  line-height: 1.3;
}

.intro-text {
  font-size: clamp(0.85rem, 3.4vw, 1rem);
  line-height: 1.5;
}

.card-label {
  font-size: clamp(1.05rem, 4.2vw, 1.4rem);
  line-height: 1.25;
}

.card-text {
  font-size: clamp(0.85rem, 3.2vw, 1rem);
  line-height: 1.55;
}

.confirmation-title {
  font-size: clamp(1.05rem, 4.2vw, 1.4rem);
  line-height: 1.25;
}

.confirmation-message {
  font-size: clamp(0.85rem, 3.2vw, 1rem);
  line-height: 1.55;
}

.confirmation-back {
  display: block;
  width: fit-content;
  margin-inline: auto;
  font-size: clamp(0.82rem, 3.2vw, 0.95rem);
  padding: clamp(0.4rem, 1.5vw, 0.6rem) 1.1rem;
}

/* Small phones (iPhone SE, Galaxy S8 / S9+, …) where the card content still
   reads a touch big. */
@media (max-width: 420px) {
  .page-title {
    font-size: 1.2rem;
  }

  .intro-title {
    font-size: 0.9rem;
  }

  .intro-text {
    font-size: 0.78rem;
  }

  .card-label {
    font-size: 0.85rem;
  }

  .card-text {
    font-size: 0.72rem;
  }

  .confirmation-title {
    font-size: 0.85rem;
  }

  .confirmation-message {
    font-size: 0.72rem;
  }

  .guest-count {
    font-size: 0.62rem;
  }
}

/* The explanatory intro appears (fades in) only once the card is fully revealed. */
.fade-enter-active {
  transition: opacity 0.6s ease;
}

.fade-enter-from {
  opacity: 0;
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
  /* Fixed height keeps the card a consistent size across loading / error /
     loaded states (no jump), and long guest lists scroll inside it. */
  height: 52%;
  /* The card itself is a fixed frame: it does not scroll, so its padding
     (incl. the bottom) is a fixed white border/line. The inner wrapper scrolls. */
  overflow: hidden;
  display: flex;
  flex-direction: column;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.96);
  box-sizing: border-box;
  /* Thinner fixed bottom line than the top/side border. */
  padding: 0.85rem 0.85rem 0.35rem;
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

/* Only the inner wrapper scrolls, so the card's bottom padding stays a fixed
   white line while the guest list scrolls underneath it. */
.invitation-sheet__scroll {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
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
  /* Layered drop-shadows follow the clipped triangle: a tight, near-opaque rim
     reads as a fine border, and a softer, offset one adds depth. */
  filter:
    drop-shadow(0 0 0.5px rgba(231, 212, 205, 1))
    drop-shadow(0 0 1px rgba(231, 212, 205, 0.95))
    drop-shadow(0 1px 1px rgba(9, 61, 87, 0.45))
    drop-shadow(0 6px 10px rgba(9, 61, 87, 0.3));
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