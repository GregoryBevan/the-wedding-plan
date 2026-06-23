<template>
  <div class="space-y-4 text-center">
    <p>Your account is not authorized to access this backoffice.</p>
    <button
      :disabled="isLoggingOut"
      class="inline-block rounded-md bg-primary px-5 py-2 text-white hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-60"
      @click="handleLogout"
    >
      {{ isLoggingOut ? 'Signing out…' : 'Logout' }}
    </button>
    <p v-if="logoutErrorMessage" class="text-sm text-red-700">{{ logoutErrorMessage }}</p>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { logout } from '../services/authApi';
import { BACKOFFICE_ROUTE_NAMES } from '../router/routeNames';

const isLoggingOut = ref(false);
const logoutErrorMessage = ref('');
const router = useRouter();

const handleLogout = async () => {
  if (isLoggingOut.value) {
    return;
  }

  isLoggingOut.value = true;
  logoutErrorMessage.value = '';

  try {
    await logout();
    await router.push({ name: BACKOFFICE_ROUTE_NAMES.signInRequired });
  } catch {
    logoutErrorMessage.value = 'Unable to sign out. Please try again.';
  } finally {
    isLoggingOut.value = false;
  }
};
</script>

