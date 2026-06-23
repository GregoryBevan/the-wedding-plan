<template>
  <div ref="containerRef">
    <div class="mb-6 flex items-center justify-between">
      <RouterLink
        :to="{ name: BACKOFFICE_ROUTE_NAMES.guestList }"
        data-test="back-to-list"
        aria-label="Back to guest list"
        class="flex h-10 w-10 items-center justify-center rounded-full border border-primary bg-primary text-2xl leading-none text-white hover:opacity-90"
      >
        <span aria-hidden="true" class="font-black">&larr;</span>
      </RouterLink>

      <h2 class="text-3xl font-light tracking-wide">Add a New Guest</h2>

      <span class="h-10 w-10" aria-hidden="true"></span>
    </div>
    <GuestForm :is-submitting="isSubmitting" @submit="handleAddGuest" />
    <p v-if="successMessage" class="mt-4 text-center text-sm text-green-700">{{ successMessage }}</p>
    <p v-if="errorMessage" class="mt-4 text-center text-sm text-red-700">{{ errorMessage }}</p>

    <div v-if="successMessage" class="mt-4 flex justify-center gap-3">
      <button
        class="rounded-md border border-secondary px-4 py-2 hover:bg-secondary/20"
        @click="clearMessages"
      >
        Add another guest
      </button>
      <RouterLink
        :to="{ name: BACKOFFICE_ROUTE_NAMES.guestList }"
        class="rounded-md bg-primary px-4 py-2 text-white hover:opacity-90"
      >
        Back to guest list
      </RouterLink>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { onBeforeRouteLeave, RouterLink } from 'vue-router';
import GuestForm from '../components/GuestForm.vue';
import { useAddRequest } from '../composables/useAddRequest';
import { addGuest, type CreateGuestPayload } from '../services/guestApi';
import { BACKOFFICE_ROUTE_NAMES } from '../router/routeNames';

const containerRef = ref<HTMLElement | null>(null);

const {
  isSubmitting,
  successMessage,
  errorMessage,
  clearMessages,
  submit
} = useAddRequest(addGuest);

const hasUnsavedFormData = (): boolean => {
  if (!containerRef.value) {
    return false;
  }

  const inputs = Array.from(containerRef.value.querySelectorAll('input')) as HTMLInputElement[];

  return inputs.some((input) => input.value.trim().length > 0);
};

const handleAddGuest = async (guestData: CreateGuestPayload) => {
  await submit(guestData, { successMessage: 'Guest added successfully.' });
};

onBeforeRouteLeave(() => {
  if (!hasUnsavedFormData()) {
    return true;
  }

  return window.confirm('You have unsaved guest details. Do you want to leave this page?');
});
</script>

