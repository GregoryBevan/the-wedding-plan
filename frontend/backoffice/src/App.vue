<template>
  <div class="min-h-screen bg-background p-8 font-serif">
    <header class="mb-10 flex items-center justify-center space-x-8 border-b border-secondary pb-8">
      <img src="./assets/logo.svg" alt="Wedding Logo" class="w-24 h-24" />
      <h1 class="text-5xl font-light tracking-widest text-text">Wedding Plan</h1>
    </header>
    <main class="mx-auto max-w-2xl bg-white p-8 rounded-xl shadow-lg border border-secondary/20 text-text">
      <h2 class="text-3xl font-light mb-6 text-center tracking-wide">Add a New Guest</h2>
      <GuestForm @submit="handleAddGuest" />
    </main>
  </div>
</template>
<script setup lang="ts">
import GuestForm from './components/GuestForm.vue';

const handleAddGuest = (guestData: { firstName: string, lastName: string }) => {
  console.log('New guest:', guestData);
  // Here you would typically call an API
};
</script>
