<template>
  <form @submit.prevent="handleSubmit" class="space-y-6">
    <BaseInput
      v-model="form.firstName"
      label="First Name"
      placeholder="e.g. John"
      required
    />
    <BaseInput
      v-model="form.lastName"
      label="Last Name"
      placeholder="e.g. Doe"
      required
    />
    <BaseButton type="submit">
      Add Guest
    </BaseButton>
  </form>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import BaseInput from './ui/BaseInput.vue';
import BaseButton from './ui/BaseButton.vue';

const emit = defineEmits(['submit']);

const form = ref({
  firstName: '',
  lastName: ''
});

const handleSubmit = () => {
  emit('submit', { ...form.value });
  // Reset form after submit for now, or keep it depending on UX
  form.value.firstName = '';
  form.value.lastName = '';
};
</script>
