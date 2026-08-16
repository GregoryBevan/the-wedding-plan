<template>
  <section>
    <header class="relative mb-6 flex items-center justify-center">
      <button
        aria-label="Back to guests"
        class="absolute left-0 inline-flex h-10 w-10 items-center justify-center rounded-full bg-primary text-white hover:opacity-90 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2"
        data-test="back-guest-details"
        type="button"
        title="Back"
        @click="navigateBack"
      >
        <img :src="backIcon" alt="" aria-hidden="true" class="h-4 w-4 brightness-0 invert" />
      </button>
      <h2 class="text-center text-3xl font-light tracking-wide text-text">Guest details</h2>
    </header>

    <p v-if="isLoading" class="py-8 text-center text-sm" aria-live="polite">Loading guest details...</p>

    <div v-else-if="errorMessage" class="space-y-3 py-8 text-center">
      <p class="text-sm text-red-700" data-test="guest-details-error" role="alert">{{ errorMessage }}</p>
      <button
        class="rounded-md bg-primary px-4 py-2 text-white hover:opacity-90 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2"
        type="button"
        @click="loadGuest"
      >
        Try again
      </button>
    </div>

    <article v-else-if="guest" class="rounded-xl border border-secondary/30 bg-white p-6 shadow-sm" data-test="guest-details-card">
      <h3 class="mb-4 text-2xl font-medium text-text" data-test="guest-details-name">{{ guest.firstName }} {{ guest.lastName }}</h3>

      <dl class="grid gap-4 text-sm text-text/80 sm:grid-cols-2">
        <div>
          <dt class="text-text/60">Email</dt>
          <dd data-test="guest-details-email">{{ guest.email }}</dd>
        </div>
        <div>
          <dt class="text-text/60">Language</dt>
          <dd data-test="guest-details-language">{{ languageLabels[guest.language] }}</dd>
        </div>
        <div>
          <dt class="text-text/60">Created at</dt>
          <dd data-test="guest-details-creation-date">{{ formatDateTime(guest.creationDate) }}</dd>
        </div>
        <div>
          <dt class="text-text/60">Updated at</dt>
          <dd data-test="guest-details-update-date">{{ formatDateTime(guest.updateDate) }}</dd>
        </div>
      </dl>

      <div class="mt-6 flex gap-3">
        <WriteOnly>
          <RouterLink
            :to="{ name: BACKOFFICE_ROUTE_NAMES.guestEdit, params: { id: guest.id }, query: { page: route.query.page, size: route.query.size } }"
            aria-label="Edit guest"
            class="flex-1 rounded bg-primary px-4 py-2 text-center text-sm text-white transition hover:opacity-90 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2"
            data-test="edit-guest-link"
          >
            Edit
          </RouterLink>
        </WriteOnly>
      </div>
    </article>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import WriteOnly from '../components/ui/WriteOnly.vue';
import backIcon from '../assets/icons/back.svg';
import { BACKOFFICE_ROUTE_NAMES } from '../router/routeNames';
import { getGuestById, type GuestLanguage, type GuestResponse } from '../services/guestApi';

const route = useRoute();
const router = useRouter();

const languageLabels: Record<GuestLanguage, string> = {
  FR: 'Français',
  EN: 'English'
};

const guestId = computed(() => String(route.params.id ?? ''));
const guest = ref<GuestResponse | null>(null);
const isLoading = ref(false);
const errorMessage = ref('');


const dateTimeFormatter = new Intl.DateTimeFormat(undefined, {
  dateStyle: 'medium',
  timeStyle: 'short'
});

const formatDateTime = (isoDate: string) => dateTimeFormatter.format(new Date(isoDate));

const navigateBack = async () => {
  if (window.history.state?.back) {
    router.back();
    return;
  }

  await router.push({ name: BACKOFFICE_ROUTE_NAMES.guestList });
};

const loadGuest = async () => {
  if (!guestId.value) {
    errorMessage.value = 'Guest not found.';
    guest.value = null;
    return;
  }

  isLoading.value = true;
  errorMessage.value = '';

  try {
    guest.value = await getGuestById(guestId.value);
  } catch (error: unknown) {
    guest.value = null;
    errorMessage.value = error instanceof Error
      ? error.message
      : 'Unexpected error while loading guest details.';
  } finally {
    isLoading.value = false;
  }
};

onMounted(() => {
  void loadGuest();
});
</script>


