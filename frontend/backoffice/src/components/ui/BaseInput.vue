<template>
  <div>
    <label v-if="label" :for="inputId" class="block text-sm font-medium uppercase tracking-wider">{{ label }}</label>
    <input
      :id="inputId"
      :name="name"
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
  inputId?: string;
  name?: string;
}>();

defineEmits(['update:modelValue']);
</script>
