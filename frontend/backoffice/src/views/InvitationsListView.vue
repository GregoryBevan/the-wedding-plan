<template>
  <section>
    <header class="mb-6 text-center">
      <h2 class="text-3xl font-light tracking-wide text-text">Invitations</h2>
    </header>

    <p v-if="isLoading" class="py-8 text-center text-sm">Loading invitations...</p>

    <div v-else-if="errorMessage" class="space-y-3 py-8 text-center">
      <p class="text-sm text-red-700">{{ errorMessage }}</p>
      <button
        class="rounded-md bg-primary px-4 py-2 text-white hover:opacity-90"
        @click="loadData"
      >
        Try again
      </button>
    </div>

    <div
      v-else-if="invitationPage.items.length === 0 && guestTotalItems === 0"
      class="space-y-4 py-8 text-center"
      data-test="empty-no-guests"
    >
      <p class="text-sm text-text/80">You need at least one guest before creating invitations.</p>
      <RouterLink
        :to="{ name: BACKOFFICE_ROUTE_NAMES.guestAdd }"
        class="inline-flex rounded-md bg-primary px-4 py-2 text-white hover:opacity-90"
        data-test="create-first-guest-cta"
      >
        Create your first guest
      </RouterLink>
    </div>

    <p
      v-else-if="invitationPage.items.length === 0"
      class="py-8 text-center text-sm text-text/80"
      data-test="empty-no-invitations"
    >
      No invitations yet.
    </p>

    <div v-else class="grid gap-4 md:grid-cols-2" data-test="invitation-card-list">
      <article
        v-for="invitation in invitationPage.items"
        :key="invitation.id"
        class="rounded-xl border border-secondary/30 bg-white p-4 shadow-sm"
        data-test="invitation-card"
      >
        <h3 class="mb-3 text-lg font-medium text-text" data-test="invitation-card-label">{{ invitation.label }}</h3>

        <p class="mb-1 text-xs text-text/60" data-test="invitation-card-guest-count">{{ invitation.guestCount }} guests</p>
        <p class="mb-3 text-xs text-text/60" data-test="invitation-card-creation-date">Created {{ formatDate(invitation.creationDate) }}</p>

        <ul class="mb-3 space-y-1 text-xs text-text/80" data-test="invitation-card-guest-details">
          <li
            v-for="guest in invitation.guests"
            :key="guest.id"
            data-test="invitation-card-guest-item"
          >
            {{ guest.firstName }} {{ guest.lastName }} - {{ guest.email }}
          </li>
        </ul>

        <div class="flex items-center justify-end gap-2">
          <button
            class="cursor-not-allowed rounded-md border border-secondary px-3 py-1 text-xs opacity-60"
            type="button"
            disabled
            aria-disabled="true"
            title="Coming soon"
          >
            View
          </button>
          <button
            class="cursor-not-allowed rounded-md border border-secondary px-3 py-1 text-xs opacity-60"
            type="button"
            disabled
            aria-disabled="true"
            title="Coming soon"
          >
            Edit
          </button>
        </div>
      </article>
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { formatDateInTimeZone } from '../composables/useDateFormatter';
import { BACKOFFICE_ROUTE_NAMES } from '../router/routeNames';
import { listGuests } from '../services/guestApi';
import { listInvitations, type InvitationPageResponse } from '../services/invitationApi';

const invitationPage = ref<InvitationPageResponse>({
  items: [],
  page: 0,
  size: 20,
  totalItems: 0,
  totalPages: 0
});
const guestTotalItems = ref(0);
const isLoading = ref(false);
const errorMessage = ref('');

const loadData = async () => {
  isLoading.value = true;
  errorMessage.value = '';

  try {
    const invitations = await listInvitations({ page: 0, size: 20 });

    invitationPage.value = invitations;

    if (invitations.items.length === 0) {
      const guests = await listGuests({ page: 0, size: 1, status: 'active' });

      guestTotalItems.value = guests.totalItems;
    }
  } catch (error: unknown) {
    errorMessage.value = error instanceof Error
      ? error.message
      : 'Unexpected error while loading invitations.';
  } finally {
    isLoading.value = false;
  }
};

const formatDate = (value: string) => formatDateInTimeZone(value);

onMounted(() => {
  void loadData();
});
</script>

