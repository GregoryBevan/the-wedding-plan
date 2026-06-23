<template>
  <div class="min-h-screen bg-background p-8 font-serif">
    <header class="mb-5 flex items-center justify-center space-x-3 border-b border-secondary pb-3">
      <img src="./assets/logo.svg" alt="Wedding Logo" class="w-14 h-14" />
      <h1 class="text-3xl font-light tracking-wide text-text">Wedding Plan</h1>
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

      <div v-else-if="!authStatus.isAuthorized" class="text-center space-y-4">
        <p>Your account is not authorized to access this backoffice.</p>
        <button
          :disabled="isLoggingOut"
          class="inline-block rounded-md bg-primary px-5 py-2 text-white hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-60"
          @click="handleLogout"
        >
          {{ isLoggingOut ? 'Signing out…' : 'Logout' }}
        </button>
        <p v-if="logoutErrorMessage" class="mt-4 text-center text-sm text-red-700">{{ logoutErrorMessage }}</p>
      </div>

      <div v-else>
        <div class="mb-6 flex justify-end">
          <button
            :disabled="isLoggingOut"
            class="inline-block rounded-md bg-primary px-5 py-2 text-white hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-60"
            @click="handleLogout"
          >
            {{ isLoggingOut ? 'Signing out…' : 'Logout' }}
          </button>
        </div>

        <nav class="mb-6 flex justify-center gap-3">
          <button
            class="rounded-md border px-4 py-2"
            :class="activeView === 'list' ? 'border-primary bg-primary text-white' : 'border-secondary hover:bg-secondary/20'"
            @click="switchView('list')"
          >
            Guest list
          </button>
          <button
            class="rounded-md border px-4 py-2"
            :class="activeView === 'add' ? 'border-primary bg-primary text-white' : 'border-secondary hover:bg-secondary/20'"
            @click="switchView('add')"
          >
            Add guest
          </button>
        </nav>

        <GuestList v-if="activeView === 'list'" />

        <div v-else>
          <h2 class="text-3xl font-light mb-6 text-center tracking-wide">Add a New Guest</h2>
          <GuestForm :is-submitting="isSubmitting" @submit="handleAddGuest" />
          <p v-if="successMessage" class="mt-4 text-center text-sm text-green-700">{{ successMessage }}</p>
          <p v-if="errorMessage" class="mt-4 text-center text-sm text-red-700">{{ errorMessage }}</p>
        </div>

        <p v-if="logoutErrorMessage" class="mt-4 text-center text-sm text-red-700">{{ logoutErrorMessage }}</p>
      </div>
    </main>
  </div>
</template>
<script setup lang="ts">
import { onMounted, ref } from 'vue';
import GuestForm from './components/GuestForm.vue';
import GuestList from './components/GuestList.vue';
import { useAddRequest } from './composables/useAddRequest';
import { addGuest, CreateGuestPayload } from './services/guestApi';
import { getAuthStatus, getGoogleLoginUrl, logout, type AuthStatus } from './services/authApi';

const isLoadingAuth = ref(true);
const authStatus = ref<AuthStatus | null>(null);
const isLoggingOut = ref(false);
const logoutErrorMessage = ref('');
const activeView = ref<'list' | 'add'>('list');

const {
  isSubmitting,
  errorMessage,
  successMessage,
  clearMessages,
  submit
} = useAddRequest(addGuest);

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

const handleAddGuest = async (guestData: CreateGuestPayload) => {
  await submit(guestData, { successMessage: 'Guest added successfully.' });
};

const handleLogout = async () => {
  if (isLoggingOut.value) {
    return;
  }

  isLoggingOut.value = true;
  logoutErrorMessage.value = '';

  try {
    await logout();
    authStatus.value = {
      isAuthenticated: false,
      email: null,
      isAuthorized: false
    };
  } catch {
    logoutErrorMessage.value = 'Unable to sign out. Please try again.';
  } finally {
    isLoggingOut.value = false;
  }
};

const switchView = (view: 'list' | 'add') => {
  if (activeView.value === view) {
    return;
  }

  clearMessages();
  activeView.value = view;
};
</script>
