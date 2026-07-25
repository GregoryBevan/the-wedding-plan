import { ref } from 'vue';

export type GuestAccessLocale = 'fr' | 'en';

type TranslationKey =
  | 'common.appName'
  | 'common.retry'
  | 'landing.title'
  | 'landing.description'
  | 'invitation.title'
  | 'invitation.errors.notFound'
  | 'invitation.errors.invalidLink'
  | 'invitation.errors.unavailable'
  | 'invitation.magicLink.request'
  | 'invitation.magicLink.sending'
  | 'invitation.magicLink.sent'
  | 'invitation.magicLink.rateLimited'
  | 'invitation.magicLink.error'
  | 'securedArea.title'
  | 'securedArea.loading'
  | 'securedArea.greeting'
  | 'securedArea.verifiedIntro'
  | 'securedArea.rsvpComingSoon'
  | 'securedArea.unverifiedTitle'
  | 'securedArea.unverified'
  | 'securedArea.error'
  | 'securedArea.restart';

const LOCALE_STORAGE_KEY = 'guest-access-locale';

const messages: Record<GuestAccessLocale, Record<TranslationKey, string>> = {
  fr: {
    'common.appName': 'Wedding Plan',
    'common.retry': 'Réessayer',
    'landing.title': 'Invitation privée',
    'landing.description': 'Scannez le QR code présent sur votre invitation pour ouvrir votre page d\'accès.',
    'invitation.title': 'Votre invitation',
    'invitation.errors.notFound': 'Cette invitation est introuvable. Vérifiez le lien de votre QR code.',
    'invitation.errors.invalidLink': 'Le lien de l\'invitation est invalide.',
    'invitation.errors.unavailable': 'Impossible de charger cette invitation pour le moment.',
    'invitation.magicLink.request': 'Recevoir mon lien d\'accès',
    'invitation.magicLink.sending': 'Envoi en cours…',
    'invitation.magicLink.sent': 'Si tout est correct, vous recevrez un email avec votre lien d\'accès.',
    'invitation.magicLink.rateLimited': 'Trop de demandes. Merci de réessayer dans quelques instants.',
    'invitation.magicLink.error': 'Impossible d\'envoyer le lien pour le moment. Réessayez.',
    'securedArea.title': 'Espace invité sécurisé',
    'securedArea.loading': 'Vérification de votre lien…',
    'securedArea.greeting': 'Bonjour',
    'securedArea.verifiedIntro': 'Votre lien a été vérifié. Vous pouvez maintenant répondre.',
    'securedArea.rsvpComingSoon': 'Le formulaire de réponse arrive bientôt.',
    'securedArea.unverifiedTitle': 'Lien expiré ou invalide',
    'securedArea.unverified': 'Votre lien d\'accès a expiré ou n\'est plus valide. Demandez-en un nouveau depuis votre invitation.',
    'securedArea.error': 'Impossible de vérifier votre session pour le moment.',
    'securedArea.restart': 'Recommencer',
  },
  en: {
    'common.appName': 'Wedding Plan',
    'common.retry': 'Try again',
    'landing.title': 'Private invitation',
    'landing.description': 'Scan the QR code on your invitation to open your access page.',
    'invitation.title': 'Your invitation',
    'invitation.errors.notFound': 'This invitation could not be found. Please check your QR code link.',
    'invitation.errors.invalidLink': 'The invitation link is invalid.',
    'invitation.errors.unavailable': 'Unable to load this invitation right now.',
    'invitation.magicLink.request': 'Get my access link',
    'invitation.magicLink.sending': 'Sending…',
    'invitation.magicLink.sent': 'If everything checks out, you will receive an email with your access link.',
    'invitation.magicLink.rateLimited': 'Too many requests. Please try again in a few moments.',
    'invitation.magicLink.error': 'We could not send the link right now. Please try again.',
    'securedArea.title': 'Secure guest area',
    'securedArea.loading': 'Verifying your link…',
    'securedArea.greeting': 'Hello',
    'securedArea.verifiedIntro': 'Your link has been verified. You can now respond.',
    'securedArea.rsvpComingSoon': 'The RSVP form is coming soon.',
    'securedArea.unverifiedTitle': 'Link expired or invalid',
    'securedArea.unverified': 'Your access link has expired or is no longer valid. Request a new one from your invitation.',
    'securedArea.error': 'We could not verify your session right now.',
    'securedArea.restart': 'Start over',
  },
};

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
    t,
    guestCountLabel,
  };
};






