import { afterEach, describe, expect, it } from 'vitest';
import { applyCapabilities, resetCapabilities, useCapabilities } from './useCapabilities';

describe('useCapabilities', () => {
  afterEach(() => {
    resetCapabilities();
  });

  it('defaults to no write capability', () => {
    expect(useCapabilities().canWrite.value).toBe(false);
  });

  it('grants the write capability from a status that can write', () => {
    applyCapabilities({ canWrite: true });

    expect(useCapabilities().canWrite.value).toBe(true);
  });

  it('revokes the write capability from a read-only status', () => {
    applyCapabilities({ canWrite: true });
    applyCapabilities({ canWrite: false });

    expect(useCapabilities().canWrite.value).toBe(false);
  });

  it('resets capabilities to the fail-closed default', () => {
    applyCapabilities({ canWrite: true });
    resetCapabilities();

    expect(useCapabilities().canWrite.value).toBe(false);
  });
});

