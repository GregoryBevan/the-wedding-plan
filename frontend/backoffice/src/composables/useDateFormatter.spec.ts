import { describe, expect, it } from 'vitest';
import { formatDateInTimeZone, normalizeUtcTimestamp } from './useDateFormatter';

describe('useDateFormatter', () => {
  it('normalizes timestamps without timezone as UTC', () => {
    expect(normalizeUtcTimestamp('2026-06-23T10:00:00')).toBe('2026-06-23T10:00:00Z');
  });

  it('keeps timestamps that already contain timezone information', () => {
    expect(normalizeUtcTimestamp('2026-06-23T10:00:00Z')).toBe('2026-06-23T10:00:00Z');
    expect(normalizeUtcTimestamp('2026-06-23T10:00:00+02:00')).toBe('2026-06-23T10:00:00+02:00');
  });

  it('formats normalized UTC dates in a provided timezone', () => {
    const actual = formatDateInTimeZone('2026-06-23T10:00:00', {
      locale: 'en-GB',
      timeZone: 'Europe/Paris'
    });

    const expected = new Intl.DateTimeFormat('en-GB', {
      dateStyle: 'medium',
      timeStyle: 'short',
      timeZone: 'Europe/Paris'
    }).format(new Date('2026-06-23T10:00:00Z'));

    expect(actual).toBe(expected);
  });

  it('returns original value when date is invalid', () => {
    expect(formatDateInTimeZone('not-a-date')).toBe('not-a-date');
  });
});

