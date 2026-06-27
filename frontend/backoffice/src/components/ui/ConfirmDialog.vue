<template>
  <teleport to="body" v-if="dialogState">
    <div class="font-serif fixed inset-0 z-50 flex items-center justify-center bg-black/40" @click.self="handleCancel">
      <div class="rounded-lg border border-secondary/40 bg-white p-6 shadow-2xl max-w-md w-full mx-4 text-text">
        <h3 class="text-lg font-medium text-text">{{ dialogState.title }}</h3>
        <p class="mt-3 text-sm text-text/80">{{ dialogState.message }}</p>

        <div class="mt-6 flex gap-3 justify-end">
          <button
            type="button"
            @click="handleCancel"
            class="rounded-md border border-secondary px-4 py-2 text-sm hover:bg-secondary/20 transition"
          >
            {{ dialogState.cancelLabel }}
          </button>
          <button
            type="button"
            @click="handleConfirm"
            :class="[
              'rounded-md px-4 py-2 text-sm text-white transition',
              dialogState.isDangerous
                ? 'bg-red-600 hover:bg-red-700'
                : 'bg-primary hover:bg-primary/90'
            ]"
          >
            {{ dialogState.confirmLabel }}
          </button>
        </div>
      </div>
    </div>
  </teleport>
</template>

<script setup lang="ts">
import { useConfirmDialog } from '../../composables/useConfirmDialog';

const { dialogState, confirm, cancel } = useConfirmDialog();

const handleConfirm = () => {
  confirm();
};

const handleCancel = () => {
  cancel();
};
</script>


