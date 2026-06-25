import { describe, expect, it } from 'vitest';
import { useConfirmDialog } from './useConfirmDialog';

describe('useConfirmDialog', () => {
  it('opens a confirmation dialog and resolves with true on confirm', async () => {
    const { openConfirm, confirm } = useConfirmDialog();

    const promise = openConfirm({
      title: 'Delete item?',
      message: 'Are you sure?'
    });

    // Simulate user clicking confirm
    setTimeout(() => {
      confirm();
    }, 0);

    const result = await promise;

    expect(result).toBe(true);
  });

  it('opens a confirmation dialog and resolves with false on cancel', async () => {
    const { openConfirm, cancel } = useConfirmDialog();

    const promise = openConfirm({
      title: 'Delete item?',
      message: 'Are you sure?'
    });

    // Simulate user clicking cancel
    setTimeout(() => {
      cancel();
    }, 0);

    const result = await promise;

    expect(result).toBe(false);
  });

  it('applies default labels when not provided', async () => {
    const { openConfirm, dialogState, confirm } = useConfirmDialog();

    const promise = openConfirm({
      title: 'Test Dialog',
      message: 'Test message'
    });

    expect(dialogState.value?.confirmLabel).toBe('Confirm');
    expect(dialogState.value?.cancelLabel).toBe('Cancel');
    expect(dialogState.value?.isDangerous).toBe(false);

    setTimeout(() => confirm(), 0);
    await promise;
  });

  it('accepts custom labels and dangerous flag', async () => {
    const { openConfirm, dialogState, confirm } = useConfirmDialog();

    const promise = openConfirm({
      title: 'Delete user?',
      message: 'This cannot be undone.',
      confirmLabel: 'Delete',
      cancelLabel: 'Keep',
      isDangerous: true
    });

    expect(dialogState.value?.confirmLabel).toBe('Delete');
    expect(dialogState.value?.cancelLabel).toBe('Keep');
    expect(dialogState.value?.isDangerous).toBe(true);

    setTimeout(() => confirm(), 0);
    await promise;
  });

  it('clears dialog state after resolution', async () => {
    const { openConfirm, cancel, dialogState } = useConfirmDialog();

    expect(dialogState.value).toBeNull();

    const promise = openConfirm({
      title: 'Test',
      message: 'Test'
    });

    expect(dialogState.value).not.toBeNull();

    setTimeout(() => cancel(), 0);
    await promise;

    expect(dialogState.value).toBeNull();
  });
});

