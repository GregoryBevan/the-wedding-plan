<template>
  <main class="min-h-dvh bg-[#f7f4f2] px-4 py-10 text-[#093D57]">
    <section class="mx-auto max-w-xl rounded-2xl bg-white p-6 shadow-sm ring-1 ring-[#d9c8c2]">
      <div class="flex items-center justify-between gap-2">
        <h1 class="text-xl font-semibold">{{ t('securedArea.title') }}</h1>
        <LanguageSwitcher />
      </div>

      <p v-if="state === 'loading'" class="mt-4 text-sm leading-6 text-[#093D57]/80" role="status">
        {{ t('securedArea.loading') }}
      </p>

      <section v-else-if="state === 'verified'" class="mt-4">
        <p v-if="session" class="text-base font-semibold text-[#093D57]">
          {{ t('securedArea.greeting') }} {{ session.firstName }} {{ session.lastName }}
        </p>
        <p class="mt-2 text-sm leading-6 text-[#093D57]/80">{{ t('securedArea.verifiedIntro') }}</p>
        <GuestRsvpForm />
      </section>

      <section v-else class="mt-4">
        <h2 class="text-base font-semibold">
          {{ state === 'error' ? t('securedArea.errorTitle') : t('securedArea.unverifiedTitle') }}
        </h2>
        <p class="mt-2 text-sm leading-6 text-[#093D57]/80">
          {{ state === 'error' ? t('securedArea.error') : t('securedArea.unverified') }}
        </p>
        <button
          v-if="state === 'error'"
          class="mt-4 w-full rounded-xl bg-[#093D57] px-4 py-2 text-sm font-semibold text-white"
          type="button"
          @click="loadSession"
        >
          {{ t('common.retry') }}
        </button>
        <RouterLink
          v-else
          class="mt-4 block w-full rounded-xl bg-[#093D57] px-4 py-2 text-center text-sm font-semibold text-white"
          :to="{ name: 'guest-access-home' }"
        >
          {{ t('securedArea.restart') }}
        </RouterLink>
      </section>
    </section>
  </main>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { RouterLink, useRoute } from 'vue-router';
import GuestRsvpForm from '../components/GuestRsvpForm.vue';
import LanguageSwitcher from '../components/LanguageSwitcher.vue';
import { fetchGuestSession, type GuestLanguage, type GuestSessionResponse } from '../services/guestAccessSecuredApi';
import { useGuestAccessI18n, type GuestAccessLocale } from '../i18n/guestAccessI18n';

type SessionState = 'loading' | 'verified' | 'unverified' | 'error';

const state = ref<SessionState>('loading');
const session = ref<GuestSessionResponse | null>(null);
const route = useRoute();
const { t, applyGuestLocale } = useGuestAccessI18n();

const guestLanguageToLocale = (language: GuestLanguage): GuestAccessLocale =>
  language === 'EN' ? 'en' : 'fr';

const loadSession = async (): Promise<void> => {
  state.value = 'loading';

  try {
    const resolvedSession = await fetchGuestSession();
    session.value = resolvedSession;

    if (resolvedSession) {
      applyGuestLocale(guestLanguageToLocale(resolvedSession.language));
    }

    state.value = resolvedSession ? 'verified' : 'unverified';
  } catch {
    state.value = 'error';
  }
};

onMounted(() => {
  // The backend redirects a failed magic-link verification (expired, already
  // used, invalid) here with `?linkStatus=invalid`. In that case there is no
  // session to fetch: show the recoverable "expired or invalid" state directly.
  if (route.query.linkStatus === 'invalid') {
    state.value = 'unverified';
    return;
  }

  void loadSession();
});
</script>

