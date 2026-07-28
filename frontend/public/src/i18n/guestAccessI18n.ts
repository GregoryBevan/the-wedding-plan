import { ref } from 'vue';

export type GuestAccessLocale = 'fr' | 'en';

type TranslationKey =
  | 'common.appName'
  | 'common.retry'
  | 'landing.title'
  | 'landing.description'
  | 'invitation.title'
  | 'invitation.chooseName'
  | 'invitation.instructions'
  | 'invitation.errors.notFound'
  | 'invitation.errors.invalidLink'
  | 'invitation.errors.unavailable'
  | 'invitation.magicLink.requestFor'
  | 'invitation.magicLink.sentTitle'
  | 'invitation.magicLink.sent'
  | 'invitation.magicLink.rateLimited'
  | 'invitation.magicLink.error'
  | 'invitation.magicLink.back'
  | 'securedArea.title'
  | 'securedArea.loading'
  | 'securedArea.greeting'
  | 'securedArea.verifiedIntro'
  | 'securedArea.rsvpComingSoon'
  | 'securedArea.unverifiedTitle'
  | 'securedArea.unverified'
  | 'securedArea.errorTitle'
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
    'invitation.chooseName': 'Dites-nous qui vous êtes ❤️',
    'invitation.instructions': 'Touchez votre prénom et nous vous enverrons un lien personnel par email pour confirmer votre présence à nos côtés.',
    'invitation.errors.notFound': 'Cette invitation est introuvable. Vérifiez le lien de votre QR code.',
    'invitation.errors.invalidLink': 'Le lien de l\'invitation est invalide.',
    'invitation.errors.unavailable': 'Impossible de charger cette invitation pour le moment.',
    'invitation.magicLink.requestFor': 'Recevoir le lien pour',
    'invitation.magicLink.sentTitle': 'Email envoyé ❤️',
    'invitation.magicLink.sent': 'Si tout est correct, vous recevrez un email avec votre lien d\'accès.',
    'invitation.magicLink.rateLimited': 'Trop de demandes. Merci de réessayer dans quelques instants.',
    'invitation.magicLink.error': 'Impossible d\'envoyer le lien pour le moment. Réessayez.',
    'invitation.magicLink.back': 'Revenir à l\'invitation',
    'securedArea.title': 'Espace invité sécurisé',
    'securedArea.loading': 'Vérification de votre lien…',
    'securedArea.greeting': 'Bonjour',
    'securedArea.verifiedIntro': 'Votre lien a été vérifié. Vous pouvez maintenant répondre.',
    'securedArea.rsvpComingSoon': 'Le formulaire de réponse arrive bientôt.',
    'securedArea.unverifiedTitle': 'Lien expiré ou invalide',
    'securedArea.unverified': 'Votre lien d\'accès a expiré ou n\'est plus valide. Demandez-en un nouveau depuis votre invitation.',
    'securedArea.errorTitle': 'Une erreur est survenue',
    'securedArea.error': 'Impossible de vérifier votre session pour le moment.',
    'securedArea.restart': 'Recommencer',
  },
  en: {
    'common.appName': 'Wedding Plan',
    'common.retry': 'Try again',
    'landing.title': 'Private invitation',
    'landing.description': 'Scan the QR code on your invitation to open your access page.',
    'invitation.title': 'Your invitation',
    'invitation.chooseName': 'Let us know who you are ❤️',
    'invitation.instructions': 'Tap your first name and we will email you a personal link to confirm you will be joining us.',
    'invitation.errors.notFound': 'This invitation could not be found. Please check your QR code link.',
    'invitation.errors.invalidLink': 'The invitation link is invalid.',
    'invitation.errors.unavailable': 'Unable to load this invitation right now.',
    'invitation.magicLink.requestFor': 'Get the access link for',
    'invitation.magicLink.sentTitle': 'Email sent ❤️',
    'invitation.magicLink.sent': 'If everything checks out, you will receive an email with your access link.',
    'invitation.magicLink.rateLimited': 'Too many requests. Please try again in a few moments.',
    'invitation.magicLink.error': 'We could not send the link right now. Please try again.',
    'invitation.magicLink.back': 'Back to the invitation',
    'securedArea.title': 'Secure guest area',
    'securedArea.loading': 'Verifying your link…',
    'securedArea.greeting': 'Hello',
    'securedArea.verifiedIntro': 'Your link has been verified. You can now respond.',
    'securedArea.rsvpComingSoon': 'The RSVP form is coming soon.',
    'securedArea.unverifiedTitle': 'Link expired or invalid',
    'securedArea.unverified': 'Your access link has expired or is no longer valid. Request a new one from your invitation.',
    'securedArea.errorTitle': 'Something went wrong',
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






