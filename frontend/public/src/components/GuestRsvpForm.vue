<template>
  <section class="mt-4">
    <p v-if="loadState === 'loading'" class="text-sm leading-6 text-[#093D57]/80" role="status">
      {{ t('rsvp.loading') }}
    </p>

    <div v-else-if="loadState === 'error'" class="rounded-xl bg-[#BEC6C2]/25 p-4">
      <p class="text-sm leading-6 text-[#093D57]/80" role="alert">{{ t('rsvp.loadError') }}</p>
      <button
        class="mt-3 w-full rounded-xl bg-[#093D57] px-4 py-2 text-sm font-semibold text-white"
        type="button"
        @click="load"
      >
        {{ t('common.retry') }}
      </button>
    </div>

    <form v-else class="flex flex-col gap-4" @submit.prevent="submit">
      <fieldset class="flex flex-col gap-2">
        <legend class="mb-2 text-base font-semibold text-[#093D57]">{{ t('rsvp.question') }}</legend>

        <label
          v-for="choice in choices"
          :key="choice"
          class="flex cursor-pointer items-center gap-3 rounded-xl border border-[#d9c8c2] px-4 py-3 text-sm text-[#093D57]"
          :class="{ 'border-[#093D57] bg-[#093D57]/5 font-semibold': selected === choice }"
        >
          <input
            class="h-4 w-4 accent-[#093D57]"
            type="radio"
            name="attendance"
            :value="choice"
            :checked="selected === choice"
            @change="select(choice)"
          />
          {{ t(choice === 'ATTENDING' ? 'rsvp.attending' : 'rsvp.declined') }}
        </label>
      </fieldset>

      <button
        class="w-full rounded-xl bg-[#093D57] px-4 py-2 text-sm font-semibold text-white disabled:opacity-50"
        type="submit"
        :disabled="!canSubmit"
      >
        {{ submitState === 'submitting' ? t('rsvp.submitting') : t('rsvp.submit') }}
      </button>

      <p v-if="submitState === 'success'" class="text-sm font-semibold text-[#093D57]" role="status">
        {{ t('rsvp.saved') }}
      </p>
      <p v-else-if="submitState === 'error'" class="text-sm text-[#a3352b]" role="alert">
        {{ t('rsvp.submitError') }}
      </p>
    </form>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { fetchRsvp, submitRsvp, type RsvpAttendance } from '../services/guestAccessSecuredApi';
import { useGuestAccessI18n } from '../i18n/guestAccessI18n';

type LoadState = 'loading' | 'ready' | 'error';
type SubmitState = 'idle' | 'submitting' | 'success' | 'error';

const choices: RsvpAttendance[] = ['ATTENDING', 'DECLINED'];

const { t } = useGuestAccessI18n();

const loadState = ref<LoadState>('loading');
const submitState = ref<SubmitState>('idle');
const saved = ref<RsvpAttendance | null>(null);
const selected = ref<RsvpAttendance | null>(null);

// Only allow submitting a genuine change: a fresh answer, or a choice that
// differs from the one already saved (so re-sending an unchanged answer is blocked).
const canSubmit = computed(
  () => selected.value !== null && selected.value !== saved.value && submitState.value !== 'submitting',
);

const load = async (): Promise<void> => {
  loadState.value = 'loading';

  try {
    const rsvp = await fetchRsvp();
    saved.value = rsvp?.attendance ?? null;
    selected.value = saved.value;
    loadState.value = 'ready';
  } catch {
    loadState.value = 'error';
  }
};

const select = (attendance: RsvpAttendance): void => {
  selected.value = attendance;

  // Clear a previous success/error banner as soon as the guest changes their mind,
  // so a stale outcome is never shown next to a different pending choice.
  if (submitState.value !== 'submitting') {
    submitState.value = 'idle';
  }
};

const submit = async (): Promise<void> => {
  // Guard against re-entrant submits (double-click / Enter) before the disabled
  // button state is reflected in the DOM, which would otherwise fire duplicate POSTs.
  if (!selected.value || submitState.value === 'submitting') {
    return;
  }

  submitState.value = 'submitting';

  try {
    const result = await submitRsvp(selected.value);
    saved.value = result.attendance;
    selected.value = result.attendance;
    submitState.value = 'success';
  } catch {
    submitState.value = 'error';
  }
};

onMounted(() => {
  void load();
});
</script>

