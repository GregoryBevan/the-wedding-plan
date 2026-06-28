<template>
  <section>
    <div class="mb-6 grid grid-cols-[1fr_auto_1fr] items-center">
      <span class="justify-self-start h-6" aria-hidden="true"></span>

      <h2 class="justify-self-center text-3xl font-light tracking-wide">{{ isArchivedView ? 'Archived Guests' : 'Guest List' }}</h2>

      <RouterLink
        v-if="!isArchivedView"
        :to="{
          name: BACKOFFICE_ROUTE_NAMES.guestAdd,
          query: {
            page: route.query.page,
            size: route.query.size
          }
        }"
        data-test="add-guest-shortcut"
        aria-label="Add a new guest"
        class="justify-self-end flex h-10 w-10 items-center justify-center rounded-full border border-primary bg-primary text-2xl leading-none text-white hover:opacity-90"
      >
        <img :src="addGuestIcon" alt="" aria-hidden="true" class="h-5 w-5 brightness-0 invert" />
      </RouterLink>
      <span v-else class="justify-self-end h-10 w-10" aria-hidden="true"></span>
    </div>

    <div class="mb-4 flex items-center justify-between text-sm text-text/80">
      <p>{{ guestCountSummary }}</p>

      <div v-if="toggleRoute" class="flex items-center">
        <RouterLink :to="toggleRoute" class="text-primary underline">
          {{ isArchivedView ? 'Switch to active' : 'Switch to archive' }}
        </RouterLink>
      </div>
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
            <th class="px-3 py-2 text-right font-semibold">Actions</th>
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
            <td class="px-3 py-2 text-right">
              <div class="inline-flex items-center justify-end gap-2">
              <RouterLink
                v-if="!isArchivedView"
                :to="{
                  name: BACKOFFICE_ROUTE_NAMES.guestEdit,
                  params: { id: guest.id },
                  query: {
                    page: route.query.page,
                    size: route.query.size
                  }
                }"
                :data-test="`edit-guest-${guest.id}`"
                aria-label="Edit guest"
                class="inline-flex h-8 w-8 items-center justify-center rounded-full bg-primary text-lg leading-none text-white hover:opacity-90"
              >
                <img :src="editIcon" alt="" aria-hidden="true" class="h-4 w-4 brightness-0 invert" />
              </RouterLink>

              <button
                v-if="!isArchivedView"
                :data-test="`archive-guest-${guest.id}`"
                aria-label="Archive guest"
                class="inline-flex h-8 w-8 items-center justify-center rounded-full bg-primary text-sm leading-none text-white hover:opacity-90"
                @click="archiveGuestById(guest.id)"
              >
                <img :src="archiveIcon" alt="" aria-hidden="true" class="h-[18px] w-[18px] brightness-0 invert" />
              </button>

              <button
                v-if="isArchivedView"
                :data-test="`restore-guest-${guest.id}`"
                aria-label="Restore guest"
                class="inline-flex h-8 w-8 items-center justify-center rounded-full bg-primary text-sm leading-none text-white hover:opacity-90"
                @click="restoreGuestById(guest.id)"
              >
                <img :src="restoreIcon" alt="" aria-hidden="true" class="h-4 w-4 brightness-0 invert" />
              </button>
              </div>
            </td>
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
import { useConfirmDialog } from '../composables/useConfirmDialog';
import { formatDateInTimeZone } from '../composables/useDateFormatter';
import { useToast } from '../composables/useToast';
import archiveIcon from '../assets/icons/archive.svg';
import addGuestIcon from '../assets/icons/add-guest.svg';
import editIcon from '../assets/icons/edit.svg';
import restoreIcon from '../assets/icons/restore.svg';
import { BACKOFFICE_ROUTE_NAMES } from '../router/routeNames';
import {
  archiveGuest,
  listGuests,
  restoreGuest,
  type GuestPageResponse,
  type GuestStatus
} from '../services/guestApi';

const props = withDefaults(defineProps<{
  status?: GuestStatus;
}>(), {
  status: 'active'
});

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
const { openConfirm } = useConfirmDialog();
const { showToast } = useToast();
let latestRequestId = 0;

const currentPage = computed(() => guestPage.value.page);
const displayedTotalPages = computed(() => Math.max(guestPage.value.totalPages, 1));
const guestCountSummary = computed(() => {
  if (guestPage.value.totalItems === 0) {
    return 'Showing 0 guests';
  }

  const start = guestPage.value.page * guestPage.value.size + 1;
  const end = start + guestPage.value.items.length - 1;

  return `Showing ${start}-${end} of ${guestPage.value.totalItems} guests`;
});
const isArchivedView = computed(() => props.status === 'archived');
const toggleRoute = computed(() => {
  if (isArchivedView.value) {
    return router.hasRoute(BACKOFFICE_ROUTE_NAMES.guestList)
      ? { name: BACKOFFICE_ROUTE_NAMES.guestList }
      : null;
  }

  return router.hasRoute(BACKOFFICE_ROUTE_NAMES.guestArchive)
    ? { name: BACKOFFICE_ROUTE_NAMES.guestArchive }
    : null;
});

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
  const requestId = ++latestRequestId;
  isLoading.value = true;
  errorMessage.value = '';

  try {
    const result = await listGuests({ page, size, status: props.status });

    if (requestId !== latestRequestId) {
      return;
    }

    guestPage.value = result;
  } catch (error: unknown) {
    if (requestId === latestRequestId) {
      errorMessage.value = error instanceof Error
        ? error.message
        : 'Unexpected error while loading guests.';
    }
  } finally {
    if (requestId === latestRequestId) {
      isLoading.value = false;
    }
  }
};

const archiveGuestById = async (id: string) => {
  const confirmed = await openConfirm({
    title: 'Archive guest',
    message: 'Are you sure you want to archive this guest?',
    confirmLabel: 'Archive',
    isDangerous: true
  });

  if (!confirmed) {
    return;
  }

  try {
    await archiveGuest(id);
    showToast('Guest archived successfully.');
    await loadGuests(currentPage.value, guestPage.value.size);
  } catch (error: unknown) {
    showToast(error instanceof Error ? error.message : 'Unable to archive guest.', 'error');
  }
};

const restoreGuestById = async (id: string) => {
  const confirmed = await openConfirm({
    title: 'Restore guest',
    message: 'Do you want to restore this guest?',
    confirmLabel: 'Restore'
  });

  if (!confirmed) {
    return;
  }

  try {
    await restoreGuest(id);
    showToast('Guest restored successfully.');
    await loadGuests(currentPage.value, guestPage.value.size);
  } catch (error: unknown) {
    showToast(error instanceof Error ? error.message : 'Unable to restore guest.', 'error');
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
