<template>
  <div class="min-h-screen bg-background p-8 font-serif">
    <header class="mb-10 flex items-center justify-center space-x-8 border-b border-secondary pb-8">
      <img src="./assets/logo.svg" alt="Wedding Logo" class="w-24 h-24" />
      <h1 class="text-5xl font-light tracking-widest text-text">Wedding Plan</h1>
    </header>
    <main class="mx-auto max-w-2xl bg-white p-8 rounded-xl shadow-lg border border-secondary/20 text-text">
      <h2 class="text-3xl font-light mb-6 text-center tracking-wide">Add a New Guest</h2>
      <GuestForm :is-submitting="isSubmitting" @submit="handleAddGuest" />
      <p v-if="successMessage" class="mt-4 text-center text-sm text-green-700">{{ successMessage }}</p>
      <p v-if="errorMessage" class="mt-4 text-center text-sm text-red-700">{{ errorMessage }}</p>
    </main>
  </div>
</template>
<script setup lang="ts">
import GuestForm from './components/GuestForm.vue';
import { useAddRequest } from './composables/useAddRequest';
import { createGuest } from './services/guestApi';

const {
  isSubmitting,
  errorMessage,
  successMessage,
  submit
} = useAddRequest(createGuest);

const handleAddGuest = async (guestData) => {
  await submit(guestData, { successMessage: 'Guest added successfully.' });
};
</script>
