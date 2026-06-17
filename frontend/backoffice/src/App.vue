<template>
  <div class="min-h-screen bg-background p-8 font-serif">
    <header class="mb-10 flex items-center justify-center space-x-8 border-b border-secondary pb-8">
      <img src="./assets/logo.svg" alt="Wedding Logo" class="w-24 h-24" />
      <h1 class="text-5xl font-light tracking-widest text-text">Wedding Plan</h1>
    </header>
    <main class="mx-auto max-w-2xl bg-white p-8 rounded-xl shadow-lg border border-secondary/20 text-text">
      <p v-if="isLoadingAuth" class="text-center text-sm">Checking authentication…</p>

      <div v-else-if="!authStatus?.isAuthenticated" class="text-center space-y-4">
        <p>Please sign in with Google to access the backoffice.</p>
        <a
          :href="getGoogleLoginUrl()"
          class="inline-block rounded-md bg-primary px-5 py-2 text-white hover:opacity-90"
        >
          Sign in with Google
        </a>
      </div>

      <div v-else-if="!authStatus.isAuthorized" class="text-center">
        <p>Your account is not authorized to access this backoffice.</p>
      </div>

      <div v-else>
        <h2 class="text-3xl font-light mb-6 text-center tracking-wide">Add a New Guest</h2>
        <GuestForm :is-submitting="isSubmitting" @submit="handleAddGuest" />
        <p v-if="successMessage" class="mt-4 text-center text-sm text-green-700">{{ successMessage }}</p>
        <p v-if="errorMessage" class="mt-4 text-center text-sm text-red-700">{{ errorMessage }}</p>
      </div>
    </main>
  </div>
</template>
<script setup lang="ts">
import { onMounted, ref } from 'vue';
import GuestForm from './components/GuestForm.vue';
import { useAddRequest } from './composables/useAddRequest';
import { addGuest } from './services/guestApi';
import { getAuthStatus, getGoogleLoginUrl } from './services/authApi';

const {
  isSubmitting,
  errorMessage,
  successMessage,
  submit
} = useAddRequest(addGuest);

const isLoadingAuth = ref(true);
const authStatus = ref<Awaited<ReturnType<typeof getAuthStatus>> | null>(null);

onMounted(async () => {
  try {
    authStatus.value = await getAuthStatus();
  } catch {
    authStatus.value = {
      isAuthenticated: false,
      email: null,
      isAuthorized: false
    };
  } finally {
    isLoadingAuth.value = false;
  }
});

const handleAddGuest = async (guestData) => {
  await submit(guestData, { successMessage: 'Guest added successfully.' });
};
</script>
