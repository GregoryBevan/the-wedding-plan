import { ref } from 'vue';
import { messages } from './messages';
import type { GuestAccessLocale, TranslationKey } from './messages';

export type { GuestAccessLocale, TranslationKey } from './messages';

const LOCALE_STORAGE_KEY = 'guest-access-locale';


const locale = ref<GuestAccessLocale>('fr');
let initialized = false;

const isSupportedLocale = (value: string): value is GuestAccessLocale =>
  value === 'fr' || value === 'en';

export const detectPreferredLocale = (languages: readonly string[]): GuestAccessLocale => {
  const firstSupported = languages
    .map((language) => language.toLowerCase())
    .find((language) => language.startsWith('fr') || language.startsWith('en'));

  if (!firstSupported) {
    return 'en';
  }

  if (firstSupported.startsWith('fr')) {
    return 'fr';
  }

  return 'en';
};

const getStoredLocale = (): GuestAccessLocale | null => {
  if (typeof window === 'undefined') {
    return null;
  }

  const storedLocale = window.localStorage.getItem(LOCALE_STORAGE_KEY);
  return storedLocale && isSupportedLocale(storedLocale) ? storedLocale : null;
};

const resolveInitialLocale = (): GuestAccessLocale => {
  const storedLocale = getStoredLocale();
  if (storedLocale) {
    return storedLocale;
  }

  if (typeof window === 'undefined') {
    return 'en';
  }

  return detectPreferredLocale(window.navigator.languages);
};

const initializeLocale = () => {
  if (initialized) {
    return;
  }

  locale.value = resolveInitialLocale();
  initialized = true;
};

export const useGuestAccessI18n = () => {
  initializeLocale();

  const setLocale = (nextLocale: GuestAccessLocale) => {
    locale.value = nextLocale;

    if (typeof window !== 'undefined') {
      window.localStorage.setItem(LOCALE_STORAGE_KEY, nextLocale);
    }
  };

  /**
   * Applies the locale advertised by the resolved guest session, unless the guest
   * has already made an explicit manual choice (persisted in local storage), which
   * always takes precedence. The applied value is intentionally not persisted so a
   * later manual switch stays authoritative.
   */
  const applyGuestLocale = (nextLocale: GuestAccessLocale) => {
    if (getStoredLocale()) {
      return;
    }

    locale.value = nextLocale;
  };

  const t = (key: TranslationKey) => messages[locale.value][key] ?? messages.en[key];

  const guestCountLabel = (guestCount: number): string => {
    if (locale.value === 'fr') {
      return `${guestCount} invité${guestCount > 1 ? 's' : ''}`;
    }

    return `${guestCount} guest${guestCount > 1 ? 's' : ''}`;
  };

  return {
    locale,
    setLocale,
    applyGuestLocale,
    t,
    guestCountLabel,
  };
};






