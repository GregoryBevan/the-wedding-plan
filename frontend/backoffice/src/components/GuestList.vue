<template>
  <section>
    <h2 class="mb-6 text-center text-3xl font-light tracking-wide">Guest List</h2>

    <div class="mb-4 text-sm text-text/80">
      <p>Showing {{ guestPage.items.length }} of {{ guestPage.totalItems }} guests</p>
    </div>

    <p v-if="isLoading" class="py-8 text-center text-sm">Loading guests...</p>

    <div v-else-if="errorMessage" class="space-y-3 py-8 text-center">
      <p class="text-sm text-red-700">{{ errorMessage }}</p>
      <button
        class="rounded-md bg-primary px-4 py-2 text-white hover:opacity-90"
        @click="loadGuests(currentPage)"
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
        @click="loadGuests(currentPage - 1)"
      >
        Previous
      </button>

      <p class="text-sm text-text/80">
        Page {{ guestPage.page + 1 }} of {{ displayedTotalPages }}
      </p>

      <button
        class="rounded-md border border-secondary px-4 py-2 hover:bg-secondary/20 disabled:cursor-not-allowed disabled:opacity-60"
        :disabled="isLoading || guestPage.page + 1 >= guestPage.totalPages"
        @click="loadGuests(currentPage + 1)"
      >
        Next
      </button>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { formatDateInTimeZone } from '../composables/useDateFormatter';
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

const currentPage = computed(() => guestPage.value.page);
const displayedTotalPages = computed(() => Math.max(guestPage.value.totalPages, 1));

const loadGuests = async (page: number) => {
  if (isLoading.value) {
    return;
  }

  isLoading.value = true;
  errorMessage.value = '';

  try {
    guestPage.value = await listGuests({ page, size: guestPage.value.size });
  } catch (error: unknown) {
    errorMessage.value = error instanceof Error
      ? error.message
      : 'Unexpected error while loading guests.';
  } finally {
    isLoading.value = false;
  }
};

const formatDate = (value: string) => formatDateInTimeZone(value);

onMounted(() => {
  void loadGuests(0);
});
</script>
