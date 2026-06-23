interface FormatDateOptions {
  locale?: string | string[];
  timeZone?: string;
}

const hasExplicitTimezone = (value: string): boolean => /(?:Z|[+-]\d{2}:\d{2})$/.test(value);

export const normalizeUtcTimestamp = (value: string): string =>
  hasExplicitTimezone(value) ? value : `${value}Z`;

export const formatDateInTimeZone = (
  value: string,
  { locale, timeZone }: FormatDateOptions = {}
): string => {
  const date = new Date(normalizeUtcTimestamp(value));

  if (Number.isNaN(date.getTime())) {
    return value;
  }

  const resolvedTimeZone = timeZone ?? Intl.DateTimeFormat().resolvedOptions().timeZone;

  return new Intl.DateTimeFormat(locale, {
    dateStyle: 'medium',
    timeStyle: 'short',
    timeZone: resolvedTimeZone
  }).format(date);
};

