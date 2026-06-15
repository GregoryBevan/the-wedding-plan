<template>
  <button
    :type="type"
    class="w-full justify-center rounded border border-transparent bg-primary py-3 px-6 text-sm font-medium text-white shadow hover:bg-secondary transition duration-300 disabled:opacity-50 disabled:cursor-not-allowed"
    v-bind="$attrs"
  >
    <slot />
  </button>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  type?: 'button' | 'submit' | 'reset';
}>(), {
  type: 'button'
});
</script>
