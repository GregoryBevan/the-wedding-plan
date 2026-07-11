<template>
  <main class="mx-auto flex min-h-dvh w-full max-w-md items-center px-4 py-8">
    <section class="w-full rounded-2xl bg-white/90 p-6 text-center shadow-xl ring-1 ring-[#093D57]/20">
      <div class="flex items-center justify-between gap-2">
        <p class="text-xs font-semibold uppercase tracking-[0.25em] text-[#093D57]">{{ t('common.appName') }}</p>
        <LanguageSwitcher />
      </div>

      <h1 class="mt-3 text-2xl font-semibold text-[#093D57]">{{ t('landing.title') }}</h1>
      <p class="mt-4 text-sm leading-6 text-[#093D57]/80">
        {{ t('landing.description') }}
      </p>
    </section>
  </main>
</template>

<script setup lang="ts">
import LanguageSwitcher from '../components/LanguageSwitcher.vue';
import { useGuestAccessI18n } from '../i18n/guestAccessI18n';

const { t } = useGuestAccessI18n();
</script>
