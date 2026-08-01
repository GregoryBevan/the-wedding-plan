import type { GuestAccessLocale, LocaleMessages } from './types';
import { fr } from './fr';
import { en } from './en';

export const messages: Record<GuestAccessLocale, LocaleMessages> = {
  fr,
  en,
};

export type { GuestAccessLocale, LocaleMessages, TranslationKey } from './types';

