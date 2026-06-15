<template>
  <div>
    <label v-if="label" class="block text-sm font-medium uppercase tracking-wider">{{ label }}</label>
    <input
      :type="type"
      :value="modelValue"
      @input="$emit('update:modelValue', ($event.target as HTMLInputElement).value)"
      class="mt-2 block w-full rounded border-gray-300 shadow-sm focus:border-primary focus:ring-primary"
      v-bind="$attrs"
    />
  </div>
</template>

<script setup lang="ts">
defineProps<{
  label?: string;
  modelValue?: string;
  type?: string;
}>();

defineEmits(['update:modelValue']);
</script>
