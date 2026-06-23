<template>
  <div class="min-h-screen bg-background p-8 font-serif">
    <header class="relative mb-5 flex items-center justify-center border-b border-secondary pb-3">
      <div class="flex items-center space-x-3">
        <img src="./assets/logo.svg" alt="Wedding Logo" class="w-14 h-14" />
        <h1 class="text-3xl font-light tracking-wide text-text">Wedding Plan</h1>
      </div>

      <div v-if="isProtectedRoute" class="absolute right-1 top-1/2 -translate-y-[70%]">
        <UserMenu
          :user-email="connectedUserEmail"
          :is-logging-out="isLoggingOut"
          :logout-error-message="logoutErrorMessage"
          @logout="handleLogout"
        />
      </div>
    </header>
    <main class="mx-auto max-w-2xl bg-white p-8 rounded-xl shadow-lg border border-secondary/20 text-text">

      <RouterView />
    </main>
  </div>
</template>
<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import { useRouter } from 'vue-router';
import UserMenu from './components/ui/UserMenu.vue';
import { getAuthStatus, logout } from './services/authApi';
import { BACKOFFICE_ROUTE_NAMES } from './router/routeNames';

const isLoggingOut = ref(false);
const logoutErrorMessage = ref('');
const connectedUserEmail = ref<string | null>(null);
const route = useRoute();
const router = useRouter();

const isProtectedRoute = computed(() => route.matched.some((record) => record.meta.requiresAuthorized));

watch(() => route.fullPath, async () => {
  if (!isProtectedRoute.value) {
    connectedUserEmail.value = null;
    return;
  }

  try {
    const status = await getAuthStatus();
    connectedUserEmail.value = status.email;
  } catch {
    connectedUserEmail.value = null;
  }
}, { immediate: true });

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
