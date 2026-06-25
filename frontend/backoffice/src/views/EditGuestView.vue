<template>
  <div>
    <div class="mb-6 flex items-center justify-between">
      <h2 class="text-3xl font-light tracking-wide">Edit Guest</h2>
    </div>

    <p v-if="isLoadingGuest" class="py-8 text-center text-sm">Loading guest...</p>
    <p v-else-if="loadErrorMessage" class="py-8 text-center text-sm text-red-700">{{ loadErrorMessage }}</p>

    <template v-else>
      <GuestForm
        :is-submitting="isSubmitting"
        :initial-values="initialValues"
        submit-label="Update Guest"
        submitting-label="Updating guest..."
        :reset-on-submit="false"
        :show-cancel-button="true"
        @submit="handleUpdateGuest"
        @cancel="handleCancel"
        @dirty-change="hasUnsavedChanges = $event"
      />
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { onBeforeRouteLeave, useRoute, useRouter } from 'vue-router';
import GuestForm from '../components/GuestForm.vue';
import { useAddRequest } from '../composables/useAddRequest';
import type { GuestFormData } from '../composables/useGuestForm';
import { useConfirmDialog } from '../composables/useConfirmDialog';
import { useToast } from '../composables/useToast';
import { getGuestById, updateGuest, type GuestResponse } from '../services/guestApi';
import { BACKOFFICE_ROUTE_NAMES } from '../router/routeNames';

const route = useRoute();
const router = useRouter();
const { openConfirm } = useConfirmDialog();
const { showToast } = useToast();

const guestId = computed(() => String(route.params.id ?? ''));
const isLoadingGuest = ref(true);
const loadErrorMessage = ref('');
const hasUnsavedChanges = ref(false);
const guestVersion = ref<number>(0);
const initialValues = ref<GuestFormData>({
  firstName: '',
  lastName: '',
  email: ''
});

const {
  isSubmitting,
  errorMessage,
  clearMessages,
  submit
} = useAddRequest(async (payload: GuestFormData) => updateGuest(guestId.value, {
  version: guestVersion.value,
  ...payload
}));

const guestListTarget = computed(() => ({
  name: BACKOFFICE_ROUTE_NAMES.guestList,
  query: {
    page: route.query.page,
    size: route.query.size
  }
}));

const syncGuestState = (guest: GuestResponse) => {
  guestVersion.value = guest.version;
  initialValues.value = {
    firstName: guest.firstName,
    lastName: guest.lastName,
    email: guest.email
  };
};

const loadGuest = async () => {
  isLoadingGuest.value = true;
  clearMessages();

  try {
    const guest = await getGuestById(guestId.value);
    syncGuestState(guest);
    hasUnsavedChanges.value = false;
    loadErrorMessage.value = '';
  } catch (error: unknown) {
    loadErrorMessage.value = error instanceof Error
      ? error.message
      : 'Unexpected error while loading guest.';
  } finally {
    isLoadingGuest.value = false;
  }
};

const handleUpdateGuest = async (guestData: GuestFormData) => {
  const updatedGuest = await submit(guestData);

  if (!updatedGuest) {
    showToast(errorMessage.value || 'Unable to update guest at the moment.', 'error');
    return;
  }

  syncGuestState(updatedGuest);
  hasUnsavedChanges.value = false;
  showToast('Guest updated successfully.', 'success');
  await router.push(guestListTarget.value);
};

onMounted(() => {
  void loadGuest();
});

const handleCancel = async () => {
  // Leave confirmation is handled by onBeforeRouteLeave.
  await router.push(guestListTarget.value);
};

onBeforeRouteLeave(async () => {
  if (!hasUnsavedChanges.value) {
    return true;
  }

  return await openConfirm({
    title: 'Unsaved Changes',
    message: 'You have unsaved guest details. Do you want to leave this page?',
    confirmLabel: 'Leave',
    cancelLabel: 'Continue editing'
  });
});
</script>

