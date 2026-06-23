<template>
  <section>
    <div class="mb-6 flex items-center justify-between">
      <span class="h-10 w-10" aria-hidden="true"></span>

      <h2 class="text-3xl font-light tracking-wide">Guest List</h2>

      <RouterLink
        :to="{ name: BACKOFFICE_ROUTE_NAMES.guestAdd }"
        data-test="add-guest-shortcut"
        aria-label="Add a new guest"
        class="flex h-10 w-10 items-center justify-center rounded-full border border-primary bg-primary text-2xl leading-none text-white hover:opacity-90"
      >
        <span aria-hidden="true" class="font-black">+</span>
      </RouterLink>
    </div>

    <div class="mb-4 text-sm text-text/80">
      <p>Showing {{ guestPage.items.length }} of {{ guestPage.totalItems }} guests</p>
    </div>

    <p v-if="isLoading" class="py-8 text-center text-sm">Loading guests...</p>

    <div v-else-if="errorMessage" class="space-y-3 py-8 text-center">
      <p class="text-sm text-red-700">{{ errorMessage }}</p>
      <button
        class="rounded-md bg-primary px-4 py-2 text-white hover:opacity-90"
        @click="loadGuests(currentPage, guestPage.size)"
      >
        Try again
      </button>
    </div>

    <p v-else-if="guestPage.items.length === 0" class="py-8 text-center text-sm text-text/80">
      No guests found.
    </p>

    <div v-else class="overflow-x-auto">
      <table class="min-w-full border-collapse text-left text-sm">
        <thead>
          <tr class="border-b border-secondary/40 text-text/90">
            <th class="px-3 py-2 font-semibold">Name</th>
            <th class="px-3 py-2 font-semibold">Email</th>
            <th class="px-3 py-2 font-semibold">Created</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="guest in guestPage.items"
            :key="guest.id"
            class="border-b border-secondary/20"
          >
            <td class="px-3 py-2">{{ guest.firstName }} {{ guest.lastName }}</td>
            <td class="px-3 py-2">{{ guest.email }}</td>
            <td class="px-3 py-2">{{ formatDate(guest.creationDate) }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="mt-6 flex items-center justify-between">
      <button
        class="rounded-md border border-secondary px-4 py-2 hover:bg-secondary/20 disabled:cursor-not-allowed disabled:opacity-60"
        :disabled="isLoading || currentPage <= 0"
        @click="updatePaginationQuery(currentPage - 1, guestPage.size)"
      >
        Previous
      </button>

      <p class="text-sm text-text/80">
        Page {{ guestPage.page + 1 }} of {{ displayedTotalPages }}
      </p>

      <button
        class="rounded-md border border-secondary px-4 py-2 hover:bg-secondary/20 disabled:cursor-not-allowed disabled:opacity-60"
        :disabled="isLoading || guestPage.page + 1 >= guestPage.totalPages"
        @click="updatePaginationQuery(currentPage + 1, guestPage.size)"
      >
        Next
      </button>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { formatDateInTimeZone } from '../composables/useDateFormatter';
import { BACKOFFICE_ROUTE_NAMES } from '../router/routeNames';
import { listGuests, type GuestPageResponse } from '../services/guestApi';

const DEFAULT_PAGE_SIZE = 10;

const guestPage = ref<GuestPageResponse>({
  items: [],
  page: 0,
  size: DEFAULT_PAGE_SIZE,
  totalItems: 0,
  totalPages: 1
});
const isLoading = ref(false);
const errorMessage = ref('');
const route = useRoute();
const router = useRouter();

const currentPage = computed(() => guestPage.value.page);
const displayedTotalPages = computed(() => Math.max(guestPage.value.totalPages, 1));

const parsePage = (value: unknown): number => {
  const numericValue = Number(value);

  if (Number.isInteger(numericValue) && numericValue >= 0) {
    return numericValue;
  }

  return 0;
};

const parseSize = (value: unknown): number => {
  const numericValue = Number(value);

  if (Number.isInteger(numericValue) && numericValue > 0) {
    return numericValue;
  }

  return DEFAULT_PAGE_SIZE;
};

const updatePaginationQuery = async (page: number, size: number, replace = false) => {
  const nextQuery = {
    ...route.query,
    page: String(page),
    size: String(size)
  };

  const currentPageValue = String(route.query.page ?? '');
  const currentSizeValue = String(route.query.size ?? '');

  if (currentPageValue === nextQuery.page && currentSizeValue === nextQuery.size) {
    return;
  }

  if (replace) {
    await router.replace({ query: nextQuery });
    return;
  }

  await router.push({ query: nextQuery });
};

const loadGuests = async (page: number, size: number) => {
  if (isLoading.value) {
    return;
  }

  isLoading.value = true;
  errorMessage.value = '';

  try {
    guestPage.value = await listGuests({ page, size });
  } catch (error: unknown) {
    errorMessage.value = error instanceof Error
      ? error.message
      : 'Unexpected error while loading guests.';
  } finally {
    isLoading.value = false;
  }
};

const formatDate = (value: string) => formatDateInTimeZone(value);

watch(() => [route.query.page, route.query.size], ([rawPage, rawSize]) => {
  const page = parsePage(rawPage);
  const size = parseSize(rawSize);
  const needsNormalizedQuery = String(rawPage ?? '') !== String(page) || String(rawSize ?? '') !== String(size);

  if (needsNormalizedQuery) {
    void updatePaginationQuery(page, size, true);
    return;
  }

  void loadGuests(page, size);
}, {
  immediate: true
});
</script>
