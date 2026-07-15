<template>
  <section class="mx-auto max-w-3xl">
    <header class="mb-6 flex items-center justify-center">
      <h2 class="text-3xl font-light tracking-wide text-text">Create invitation</h2>
    </header>

    <form class="space-y-5" @submit.prevent="handleSubmit">
      <div>
        <label class="mb-1 block text-sm font-medium text-text" for="invitation-label">Label</label>
        <input
          id="invitation-label"
          v-model="label"
          class="w-full rounded-md border border-secondary/50 px-3 py-2 text-sm"
          data-test="invitation-label-input"
          required
          type="text"
        >
      </div>

      <div>
        <label class="mb-1 block text-sm font-medium text-text" for="invitation-description">Description</label>
        <textarea
          id="invitation-description"
          v-model="description"
          class="min-h-24 w-full rounded-md border border-secondary/50 px-3 py-2 text-sm"
          data-test="invitation-description-input"
        />
      </div>

      <div class="rounded-xl border border-secondary/30 bg-white p-4 shadow-sm">
        <div class="mb-3 flex items-center justify-between gap-3">
          <h3 class="text-base font-medium text-text">Guests</h3>
          <input
            id="guest-search"
            v-model="searchQuery"
            aria-label="Search guests"
            class="w-56 rounded-md border border-secondary/50 px-3 py-2 text-sm"
            data-test="guest-search-input"
            placeholder="Search guest"
            type="search"
          >
        </div>

        <p v-if="isLoadingGuests" class="text-sm text-text/70">Loading guests...</p>
        <p v-else-if="guestLoadErrorMessage" class="text-sm text-red-700" data-test="guest-load-error">{{ guestLoadErrorMessage }}</p>

        <div
          v-else
          class="max-h-64 space-y-2 overflow-y-auto"
          data-test="guest-options-container"
          @scroll.passive="handleGuestListScroll"
        >
          <label
            v-for="guest in filteredGuests"
            :key="guest.id"
            class="flex cursor-pointer items-center gap-2 rounded-md border border-transparent px-2 py-1 hover:border-secondary/30"
            data-test="guest-checkbox-option"
          >
            <input
              v-model="selectedGuestIds"
              :value="guest.id"
              data-test="guest-checkbox"
              type="checkbox"
            >
            <span class="text-sm text-text">{{ guest.firstName }} {{ guest.lastName }} - {{ guest.email }}</span>
          </label>

          <div v-if="guests.length === 0 && !normalizedSearchQuery" class="space-y-2 text-sm text-text/70" data-test="empty-no-guests-available">
            <p>No guests available yet.</p>
            <RouterLink
              :to="{ name: BACKOFFICE_ROUTE_NAMES.guestAdd }"
              aria-label="Create guest"
              class="inline-flex h-8 w-8 items-center justify-center rounded-full bg-primary text-white hover:opacity-90 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2"
              data-test="create-guest-link"
              title="Create guest"
            >
              <img :src="addGuestIcon" alt="" aria-hidden="true" class="h-4 w-4 brightness-0 invert" />
            </RouterLink>
          </div>
          <p
            v-else-if="filteredGuests.length === 0"
            class="text-sm text-text/70"
            data-test="empty-no-search-match"
          >
            No guests match your search.
          </p>

          <p v-if="isLoadingMoreGuests" class="text-sm text-text/70" data-test="guests-loading-more">
            Loading more guests...
          </p>
        </div>
      </div>

      <p v-if="validationErrorMessage" class="text-sm text-red-700" data-test="invitation-validation-error">
        {{ validationErrorMessage }}
      </p>
      <p v-if="submitErrorMessage" class="text-sm text-red-700" data-test="invitation-submit-error">
        {{ submitErrorMessage }}
      </p>

      <div class="flex gap-3 pt-2">
        <RouterLink
          :to="{ name: BACKOFFICE_ROUTE_NAMES.invitationList }"
          class="flex-1 rounded border border-secondary px-4 py-3 text-center text-sm transition hover:bg-secondary/20"
        >
          Cancel
        </RouterLink>
        <button
          :disabled="isSubmitting"
          class="flex-1 rounded bg-primary px-4 py-3 text-sm text-white transition hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-60"
          data-test="create-invitation-submit"
          type="submit"
        >
          {{ isSubmitting ? 'Creating invitation...' : 'Create invitation' }}
        </button>
      </div>
    </form>
  </section>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import addGuestIcon from '../assets/icons/add-guest.svg';
import { BACKOFFICE_ROUTE_NAMES } from '../router/routeNames';
import { listGuests, type GuestResponse } from '../services/guestApi';
import { createInvitation } from '../services/invitationApi';

const router = useRouter();

const guests = ref<GuestResponse[]>([]);
const selectedGuestIds = ref<string[]>([]);
const label = ref('');
const description = ref('');
const searchQuery = ref('');
const isLoadingGuests = ref(false);
const isLoadingMoreGuests = ref(false);
const isSubmitting = ref(false);
const guestLoadErrorMessage = ref('');
const validationErrorMessage = ref('');
const submitErrorMessage = ref('');
const guestPage = ref(0);
const totalGuestPages = ref(1);
let guestSearchDebounceTimer: ReturnType<typeof setTimeout> | undefined;
let latestGuestRequestId = 0;

const GUEST_PAGE_SIZE = 10;
const GUEST_LIST_SCROLL_THRESHOLD_PX = 24;
const GUEST_SEARCH_DEBOUNCE_MS = 300;

const hasMoreGuests = computed(() => guestPage.value + 1 < totalGuestPages.value);
const normalizedSearchQuery = computed(() => searchQuery.value.trim());

const filteredGuests = computed(() => {
  return guests.value;
});

const loadGuests = async ({ reset = false }: { reset?: boolean } = {}) => {
  guestLoadErrorMessage.value = '';

  if (!reset && (isLoadingGuests.value || isLoadingMoreGuests.value)) {
    return;
  }

  if (reset) {
    guests.value = [];
    guestPage.value = 0;
    totalGuestPages.value = 1;
  }

  const pageToLoad = guestPage.value === 0 && guests.value.length === 0
    ? 0
    : guestPage.value + 1;

  const isFirstPage = pageToLoad === 0;

  if (!isFirstPage && !hasMoreGuests.value) {
    return;
  }

  if (isFirstPage) {
    isLoadingGuests.value = true;
    isLoadingMoreGuests.value = false;
  } else {
    isLoadingMoreGuests.value = true;
  }

  const requestId = ++latestGuestRequestId;
  const normalizedSearch = normalizedSearchQuery.value;

  try {
    const response = await listGuests({
      page: pageToLoad,
      size: GUEST_PAGE_SIZE,
      status: 'active',
      availability: 'unassigned',
      search: normalizedSearch || undefined
    });

    if (requestId !== latestGuestRequestId) {
      return;
    }

    if (isFirstPage) {
      guests.value = response.items;
    } else {
      guests.value = [...guests.value, ...response.items];
    }

    guestPage.value = response.page;
    totalGuestPages.value = response.totalPages;
  } catch (error: unknown) {
    if (requestId !== latestGuestRequestId) {
      return;
    }

    guestLoadErrorMessage.value = error instanceof Error
      ? error.message
      : 'Unable to retrieve guests at the moment.';
  } finally {
    if (requestId !== latestGuestRequestId) {
      return;
    }

    if (isFirstPage) {
      isLoadingGuests.value = false;
    } else {
      isLoadingMoreGuests.value = false;
    }
  }
};

const handleGuestListScroll = async (event: Event) => {
  if (isLoadingGuests.value || isLoadingMoreGuests.value || !hasMoreGuests.value) {
    return;
  }

  const target = event.target as HTMLElement | null;

  if (!target) {
    return;
  }

  const isNearBottom = target.scrollTop + target.clientHeight >= target.scrollHeight - GUEST_LIST_SCROLL_THRESHOLD_PX;

  if (isNearBottom) {
    await loadGuests();
  }
};

watch(searchQuery, () => {
  if (guestSearchDebounceTimer) {
    clearTimeout(guestSearchDebounceTimer);
  }

  guestSearchDebounceTimer = setTimeout(() => {
    void loadGuests({ reset: true });
  }, GUEST_SEARCH_DEBOUNCE_MS);
});

onBeforeUnmount(() => {
  if (guestSearchDebounceTimer) {
    clearTimeout(guestSearchDebounceTimer);
  }
});

const handleSubmit = async () => {
  validationErrorMessage.value = '';
  submitErrorMessage.value = '';

  if (label.value.trim().length === 0) {
    validationErrorMessage.value = 'Label is required.';
    return;
  }

  if (selectedGuestIds.value.length === 0) {
    validationErrorMessage.value = 'Select at least one guest.';
    return;
  }

  isSubmitting.value = true;

  try {
    await createInvitation({
      label: label.value.trim(),
      description: description.value.trim(),
      guestIds: selectedGuestIds.value
    });

    await router.push({ name: BACKOFFICE_ROUTE_NAMES.invitationList });
  } catch (error: unknown) {
    submitErrorMessage.value = error instanceof Error
      ? error.message
      : 'Unable to create invitation at the moment.';
  } finally {
    isSubmitting.value = false;
  }
};

onMounted(() => {
  void loadGuests();
});
</script>

