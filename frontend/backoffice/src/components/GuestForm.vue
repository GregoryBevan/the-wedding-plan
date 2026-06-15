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
    <BaseInput
      v-model="form.email"
      type="email"
      label="Email"
      placeholder="e.g. john.doe@email.com"
      required
    />
    <BaseButton type="submit" :disabled="props.isSubmitting">
      {{ props.isSubmitting ? 'Adding guest...' : 'Add Guest' }}
    </BaseButton>
  </form>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import BaseInput from './ui/BaseInput.vue';
import BaseButton from './ui/BaseButton.vue';

interface GuestFormData {
  firstName: string;
  lastName: string;
  email: string;
}

const props = withDefaults(defineProps<{
  isSubmitting?: boolean;
}>(), {
  isSubmitting: false
});

const emit = defineEmits<{
  (e: 'submit', payload: GuestFormData): void;
}>();

const form = ref({
  firstName: '',
  lastName: '',
  email: ''
});

const handleSubmit = () => {
  if (props.isSubmitting) {
    return;
  }

  emit('submit', { ...form.value });
  // Reset form after submit for now, or keep it depending on UX
  form.value.firstName = '';
  form.value.lastName = '';
  form.value.email = '';
};
</script>
