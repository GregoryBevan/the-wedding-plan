<template>
  <section>
    <header class="relative mb-6 flex items-center justify-center">
      <RouterLink
        :to="{ name: BACKOFFICE_ROUTE_NAMES.invitationList }"
        aria-label="Back to invitations"
        class="absolute left-0 inline-flex h-10 w-10 items-center justify-center rounded-full bg-primary text-white hover:opacity-90 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2"
        title="Back"
      >
        <img :src="backIcon" alt="" aria-hidden="true" class="h-4 w-4 brightness-0 invert" />
      </RouterLink>
      <h2 class="text-center text-3xl font-light tracking-wide text-text">Invitation details</h2>
    </header>

    <p v-if="isLoading" class="py-8 text-center text-sm" aria-live="polite">Loading invitation details...</p>

    <div v-else-if="errorMessage" class="space-y-3 py-8 text-center">
      <p class="text-sm text-red-700" data-test="invitation-details-error" role="alert">{{ errorMessage }}</p>
      <button
        class="rounded-md bg-primary px-4 py-2 text-white hover:opacity-90 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2"
        type="button"
        @click="loadInvitation"
      >
        Try again
      </button>
    </div>

    <article v-else-if="invitation" class="rounded-xl border border-secondary/30 bg-white p-6 shadow-sm" data-test="invitation-details-card">
      <h3 class="mb-4 text-2xl font-medium text-text" data-test="invitation-details-label">{{ invitation.label }}</h3>

      <dl class="grid gap-4 text-sm text-text/80 sm:grid-cols-2">
        <div>
          <dt class="text-text/60">Description</dt>
          <dd data-test="invitation-details-description">{{ invitation.description }}</dd>
        </div>
        <div>
          <dt class="text-text/60">Guests</dt>
          <dd data-test="invitation-details-guest-count">{{ invitation.guestCount }}</dd>
        </div>
        <div>
          <dt class="text-text/60">Created at</dt>
          <dd data-test="invitation-details-creation-date">{{ formatDateTime(invitation.creationDate) }}</dd>
        </div>
        <div>
          <dt class="text-text/60">Updated at</dt>
          <dd data-test="invitation-details-update-date">{{ formatDateTime(invitation.updateDate) }}</dd>
        </div>
      </dl>

      <div class="mt-6">
        <h4 class="text-sm font-medium text-text/80">Guest list</h4>
        <p
          v-if="invitation.guests.length === 0"
          class="mt-2 text-sm text-text/60"
          data-test="invitation-details-guests-empty"
        >
          No guests assigned to this invitation.
        </p>
        <ul v-else class="mt-2 space-y-1 text-sm text-text/80" data-test="invitation-details-guests-list">
          <li
            v-for="guest in invitation.guests"
            :key="guest.id"
            data-test="invitation-details-guest-item"
          >
            {{ guest.firstName }} {{ guest.lastName }} ({{ guest.email }})
          </li>
        </ul>
      </div>

      <div class="mt-6 flex gap-3">
        <RouterLink
          :to="{ name: BACKOFFICE_ROUTE_NAMES.invitationEdit, params: { id: invitation.id } }"
          aria-label="Edit invitation"
          class="flex-1 rounded bg-primary px-4 py-2 text-center text-sm text-white transition hover:opacity-90 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2"
          data-test="edit-invitation-link"
        >
          Edit
        </RouterLink>
      </div>
    </article>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import backIcon from '../assets/icons/back.svg';
import { BACKOFFICE_ROUTE_NAMES } from '../router/routeNames';
import { getInvitationById, type InvitationResponse } from '../services/invitationApi';

const route = useRoute();

const invitationId = computed(() => String(route.params.id ?? ''));
const invitation = ref<InvitationResponse | null>(null);
const isLoading = ref(false);
const errorMessage = ref('');

const dateTimeFormatter = new Intl.DateTimeFormat(undefined, {
  dateStyle: 'medium',
  timeStyle: 'short'
});

const formatDateTime = (isoDate: string) => dateTimeFormatter.format(new Date(isoDate));

const loadInvitation = async () => {
  if (!invitationId.value) {
    errorMessage.value = 'Invitation not found.';
    invitation.value = null;
    return;
  }

  isLoading.value = true;
  errorMessage.value = '';

  try {
    invitation.value = await getInvitationById(invitationId.value);
  } catch (error: unknown) {
    invitation.value = null;
    errorMessage.value = error instanceof Error
      ? error.message
      : 'Unexpected error while loading invitation details.';
  } finally {
    isLoading.value = false;
  }
};

onMounted(() => {
  void loadInvitation();
});
</script>
