<template>
  <div class="language-switcher" role="group" aria-label="Language switcher">
    <button
      v-for="availableLocale in locales"
      :key="availableLocale"
      class="language-switcher__button"
      :class="{ 'language-switcher__button--active': localeValue === availableLocale }"
      type="button"
      :lang="availableLocale"
      :aria-label="availableLocale === 'fr' ? 'Français' : 'English'"
      :aria-pressed="localeValue === availableLocale"
      @click="setLocale(availableLocale)"
    >
      {{ localeLabels[availableLocale] }}
    </button>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useGuestAccessI18n, type GuestAccessLocale } from '../i18n/guestAccessI18n';

const locales: GuestAccessLocale[] = ['fr', 'en'];
const localeLabels: Record<GuestAccessLocale, string> = {
  fr: 'FR',
  en: 'EN',
};

const { locale, setLocale } = useGuestAccessI18n();
const localeValue = computed(() => locale.value);
</script>

<style scoped>
.language-switcher {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  border-radius: 999px;
  border: 1px solid rgba(9, 61, 87, 0.2);
  background: rgba(255, 255, 255, 0.65);
  padding: 0.2rem;
}

.language-switcher__button {
  min-width: 2.2rem;
  border-radius: 999px;
  border: none;
  background: transparent;
  padding: 0.3rem 0.45rem;
  font-size: 0.72rem;
  font-weight: 700;
  letter-spacing: 0.06em;
  color: rgba(9, 61, 87, 0.75);
}

.language-switcher__button--active {
  background: #093d57;
  color: #ffffff;
}
</style>




