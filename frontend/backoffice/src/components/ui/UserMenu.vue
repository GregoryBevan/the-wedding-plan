<template>
  <div ref="menuRef" class="relative">
    <button
      type="button"
      data-test="user-menu-toggle" aria-label="Open user menu" aria-haspopup="menu" :aria-expanded="isOpen"
      class="flex items-center rounded-full border border-secondary/50 bg-white p-1.5 hover:bg-secondary/10"
      :disabled="props.isLoggingOut"
      @click="toggleMenu"
    >
      <span class="flex h-9 w-9 items-center justify-center rounded-full bg-primary text-sm text-white">{{ avatarInitial }}</span>
    </button>

    <div
      v-if="isOpen"
      role="menu"
      class="absolute right-0 top-full z-10 mt-1 w-72 rounded-lg border border-secondary/40 bg-white p-4 shadow-lg"
    >
      <p class="text-xs uppercase tracking-wide text-text/70">Signed in as</p>
      <p class="mt-1 truncate text-sm font-medium">{{ userLabel }}</p>

      <button
        type="button"
        role="menuitem"
        data-test="user-menu-logout"
        class="mt-4 w-full rounded-md bg-primary px-4 py-2 text-sm font-normal text-white hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-60"
        :disabled="props.isLoggingOut"
        @click="handleLogout"
      >
        {{ props.isLoggingOut ? 'Signing out…' : 'Logout' }}
      </button>

      <p v-if="props.logoutErrorMessage" class="mt-3 text-sm text-red-700">{{ props.logoutErrorMessage }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';

const props = defineProps<{
  userEmail: string | null;
  isLoggingOut: boolean;
  logoutErrorMessage: string;
}>();

const emit = defineEmits<{
  (e: 'logout'): void;
}>();

const isOpen = ref(false);
const menuRef = ref<HTMLElement | null>(null);

const avatarInitial = computed(() => props.userEmail?.charAt(0).toUpperCase() ?? '?');
const userLabel = computed(() => props.userEmail ?? 'Unknown user');

const toggleMenu = () => {
  isOpen.value = !isOpen.value;
};

const handleLogout = () => {
  emit('logout');
};

const handleDocumentClick = (event: MouseEvent) => {
  if (!isOpen.value) {
    return;
  }

  const target = event.target;

  if (!(target instanceof Node)) {
    return;
  }

  if (!menuRef.value?.contains(target)) {
    isOpen.value = false;
  }
};

onMounted(() => {
  document.addEventListener('click', handleDocumentClick);
});

onBeforeUnmount(() => {
  document.removeEventListener('click', handleDocumentClick);
});
</script>




