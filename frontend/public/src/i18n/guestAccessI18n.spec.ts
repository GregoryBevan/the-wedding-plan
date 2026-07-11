import { beforeEach, describe, expect, it, vi } from 'vitest';

const importI18nModule = async () => import('./guestAccessI18n');

describe('guestAccessI18n', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
    vi.resetModules();
    window.localStorage.clear();
  });

  it('detects French when preferred language starts with fr', async () => {
    const { detectPreferredLocale } = await importI18nModule();

    expect(detectPreferredLocale(['fr-FR', 'en-US'])).toBe('fr');
  });

  it('falls back to English when no supported language is found', async () => {
    const { detectPreferredLocale } = await importI18nModule();

    expect(detectPreferredLocale(['es-ES', 'de-DE'])).toBe('en');
  });

  it('uses browser default locale on first load and allows manual switch', async () => {
    vi.spyOn(window.navigator, 'languages', 'get').mockReturnValue(['en-US']);

    const { useGuestAccessI18n } = await importI18nModule();

    const { locale, setLocale, t, guestCountLabel } = useGuestAccessI18n();

    expect(locale.value).toBe('en');
    expect(t('landing.title')).toBe('Private invitation');
    expect(guestCountLabel(2)).toBe('2 guests');

    setLocale('fr');

    expect(locale.value).toBe('fr');
    expect(window.localStorage.getItem('guest-access-locale')).toBe('fr');
    expect(t('landing.title')).toBe('Invitation privée');
    expect(guestCountLabel(2)).toBe('2 invités');

  });
});





