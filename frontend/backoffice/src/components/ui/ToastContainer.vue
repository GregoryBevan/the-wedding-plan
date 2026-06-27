<template>
  <teleport to="body">
    <div class="pointer-events-none fixed bottom-4 right-4 z-50 flex max-w-xs flex-col gap-2 font-serif">
      <div
        v-for="toast in toasts"
        :key="toast.id"
        class="pointer-events-auto relative rounded-lg border px-3 py-2 pr-8 shadow-lg"
        :class="toast.type === 'success'
          ? 'border-green-300 bg-green-50 text-green-900'
          : 'border-red-300 bg-red-50 text-red-900'"
        data-test="toast-message"
      >
        <p class="text-sm leading-5">{{ toast.message }}</p>
        <button
          type="button"
          class="absolute right-1 top-1 rounded px-1.5 py-0.5 text-xs leading-none hover:bg-black/5"
          @click="dismissToast(toast.id)"
          aria-label="Dismiss notification"
        >
          x
        </button>
      </div>
    </div>
  </teleport>
</template>

<script setup lang="ts">
import { useToast } from '../../composables/useToast';

const { toasts, dismissToast } = useToast();
</script>

