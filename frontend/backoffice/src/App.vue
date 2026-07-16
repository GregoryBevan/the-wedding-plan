<template>
  <div class="flex h-screen flex-col overflow-hidden bg-background p-8 font-serif" data-test="backoffice-shell">
    <header class="relative z-30 mb-5 flex shrink-0 items-center justify-center border-b border-secondary pb-3">
      <div class="flex items-center space-x-3">
        <img src="./assets/logo.svg" alt="Wedding Logo" class="w-14 h-14" />
        <h1 class="text-3xl font-light tracking-wide text-text">Wedding Plan</h1>
      </div>

      <div v-if="isProtectedRoute" class="absolute right-1 top-1/2 z-40 -translate-y-[70%]">
        <UserMenu
          :user-email="connectedUserEmail"
          :is-logging-out="isLoggingOut"
          :logout-error-message="logoutErrorMessage"
          @logout="handleLogout"
        />
      </div>
    </header>
    <div class="mx-auto flex min-h-0 w-full max-w-6xl flex-1 items-stretch gap-6" data-test="backoffice-content-shell">
      <aside
        v-if="isProtectedRoute"
        class="w-64 shrink-0 rounded-xl border border-secondary/20 bg-white p-5 text-text shadow-lg"
        data-test="backoffice-sidebar"
      >
        <BackofficeSidebar />
      </aside>

      <main class="relative z-0 h-full min-h-0 min-w-0 flex-1 overflow-auto rounded-xl border border-secondary/20 bg-white p-8 text-text shadow-lg" data-test="backoffice-main">
        <RouterView />
      </main>
    </div>
    <footer class="mx-auto mt-3 max-w-6xl shrink-0 text-right text-xs text-text/60" data-test="backoffice-version">
      Backoffice version {{ appVersion }}
    </footer>
    <ConfirmDialog />
    <ToastContainer />
  </div>
</template>
<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import BackofficeSidebar from './components/BackofficeSidebar.vue';
import UserMenu from './components/ui/UserMenu.vue';
import ConfirmDialog from './components/ui/ConfirmDialog.vue';
import ToastContainer from './components/ui/ToastContainer.vue';
import { logout } from './services/authApi';
import { clearSessionAuthStatus, getSessionAuthStatus } from './services/authStatusCache';
import { BACKOFFICE_ROUTE_NAMES } from './router/routeNames';

const isLoggingOut = ref(false);
const logoutErrorMessage = ref('');
const connectedUserEmail = ref<string | null>(null);
const route = useRoute();
const router = useRouter();
const appVersion = __APP_VERSION__;

const isProtectedRoute = computed(() => route.matched.some((record) => record.meta.requiresAuthorized));

watch(isProtectedRoute, async (isNowProtected, _, onInvalidate) => {
  let isInvalidated = false;
  onInvalidate(() => {
    isInvalidated = true;
  });

  if (!isNowProtected) {
    if (!isInvalidated) {
      connectedUserEmail.value = null;
    }
    return;
  }

  try {
    const status = await getSessionAuthStatus();

    if (!isInvalidated) {
      connectedUserEmail.value = status.email;
    }
  } catch {
    if (!isInvalidated) {
      connectedUserEmail.value = null;
    }
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
    clearSessionAuthStatus();
    connectedUserEmail.value = null;
    await router.push({ name: BACKOFFICE_ROUTE_NAMES.signInRequired });
  } catch {
    logoutErrorMessage.value = 'Unable to sign out. Please try again.';
  } finally {
    isLoggingOut.value = false;
  }
};
</script>
