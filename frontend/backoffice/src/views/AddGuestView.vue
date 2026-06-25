<template>
  <div>
    <div class="mb-6 flex items-center justify-center">
      <h2 class="text-3xl font-light tracking-wide">Add a New Guest</h2>
    </div>
    <GuestForm
      :is-submitting="isSubmitting"
      submit-label="Add Guest"
      submitting-label="Adding guest..."
      :show-cancel-button="true"
      :reset-on-submit="true"
      @cancel="handleCancel"
      @submit="handleAddGuest"
      @dirty-change="hasUnsavedChanges = $event"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { onBeforeRouteLeave, useRoute, useRouter } from 'vue-router';
import GuestForm from '../components/GuestForm.vue';
import { useAddRequest } from '../composables/useAddRequest';
import { useConfirmDialog } from '../composables/useConfirmDialog';
import { useToast } from '../composables/useToast';
import { addGuest, type CreateGuestPayload } from '../services/guestApi';
import { BACKOFFICE_ROUTE_NAMES } from '../router/routeNames';

const hasUnsavedChanges = ref(false);
const route = useRoute();
const router = useRouter();
const { openConfirm } = useConfirmDialog();
const { showToast } = useToast();

const {
  isSubmitting,
  errorMessage,
  submit
} = useAddRequest(addGuest);

const guestListTarget = computed(() => ({
  name: BACKOFFICE_ROUTE_NAMES.guestList,
  query: {
    page: route.query.page,
    size: route.query.size
  }
}));

const handleAddGuest = async (guestData: CreateGuestPayload) => {
  const createdGuest = await submit(guestData);

  if (!createdGuest) {
    showToast(errorMessage.value || 'Unable to create guest at the moment.', 'error');
    return;
  }

  hasUnsavedChanges.value = false;
  showToast('Guest added successfully.', 'success');
  await router.push(guestListTarget.value);
};

const handleCancel = async () => {
  // Let onBeforeRouteLeave own confirmation to avoid duplicate dialogs.
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

