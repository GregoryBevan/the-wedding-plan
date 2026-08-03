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

      <fieldset v-if="selected === 'ATTENDING'" class="flex flex-col gap-2">
        <legend class="mb-2 text-base font-semibold text-[#093D57]">{{ t('rsvp.meal.question') }}</legend>

        <label
          v-for="meal in meals"
          :key="meal"
          class="flex cursor-pointer items-center gap-3 rounded-xl border border-[#d9c8c2] px-4 py-3 text-sm text-[#093D57]"
          :class="{ 'border-[#093D57] bg-[#093D57]/5 font-semibold': selectedMeal === meal }"
        >
          <input
            class="h-4 w-4 accent-[#093D57]"
            type="radio"
            name="meal"
            :value="meal"
            :checked="selectedMeal === meal"
            @change="selectMeal(meal)"
          />
          {{ t(mealLabels[meal]) }}
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
import { fetchRsvp, submitRsvp, type Meal, type RsvpAttendance } from '../services/guestAccessSecuredApi';
import { useGuestAccessI18n } from '../i18n/guestAccessI18n';
import type { TranslationKey } from '../i18n/messages/types';

type LoadState = 'loading' | 'ready' | 'error';
type SubmitState = 'idle' | 'submitting' | 'success' | 'error';

const choices: RsvpAttendance[] = ['ATTENDING', 'DECLINED'];
const meals: Meal[] = ['MEAT', 'FISH', 'VEGGIE'];
const mealLabels: Record<Meal, TranslationKey> = {
  MEAT: 'rsvp.meal.meat',
  FISH: 'rsvp.meal.fish',
  VEGGIE: 'rsvp.meal.veggie',
};

const { t } = useGuestAccessI18n();

const loadState = ref<LoadState>('loading');
const submitState = ref<SubmitState>('idle');
const saved = ref<RsvpAttendance | null>(null);
const selected = ref<RsvpAttendance | null>(null);
const savedMeal = ref<Meal | null>(null);
const selectedMeal = ref<Meal | null>(null);

// A meal is mandatory to attend, so an attending answer without one can never be submitted.
const mealSatisfied = computed(() => selected.value !== 'ATTENDING' || selectedMeal.value !== null);

// A genuine change means a different attendance, or — when attending — a different meal.
const changed = computed(() => {
  if (selected.value !== saved.value) {
    return true;
  }

  return selected.value === 'ATTENDING' && selectedMeal.value !== savedMeal.value;
});

// Only allow submitting a valid, genuine change that is not already in flight.
const canSubmit = computed(
  () =>
    selected.value !== null && mealSatisfied.value && changed.value && submitState.value !== 'submitting',
);

const load = async (): Promise<void> => {
  loadState.value = 'loading';

  try {
    const rsvp = await fetchRsvp();
    saved.value = rsvp?.attendance ?? null;
    selected.value = saved.value;
    savedMeal.value = rsvp?.meal ?? null;
    selectedMeal.value = savedMeal.value;
    loadState.value = 'ready';
  } catch {
    loadState.value = 'error';
  }
};

// Clear a previous success/error banner as soon as the guest changes their mind,
// so a stale outcome is never shown next to a different pending choice.
const clearOutcome = (): void => {
  if (submitState.value !== 'submitting') {
    submitState.value = 'idle';
  }
};

const select = (attendance: RsvpAttendance): void => {
  selected.value = attendance;
  clearOutcome();
};

const selectMeal = (meal: Meal): void => {
  selectedMeal.value = meal;
  clearOutcome();
};

const submit = async (): Promise<void> => {
  // Guard against re-entrant submits (double-click / Enter) and invalid/unchanged
  // answers before the disabled button state is reflected in the DOM.
  if (!canSubmit.value) {
    return;
  }

  submitState.value = 'submitting';

  try {
    // `canSubmit` guarantees a meal is chosen before an attending answer is sent.
    const result =
      selected.value === 'ATTENDING'
        ? await submitRsvp('ATTENDING', selectedMeal.value!)
        : await submitRsvp('DECLINED');
    saved.value = result.attendance;
    selected.value = result.attendance;
    savedMeal.value = result.meal ?? null;
    selectedMeal.value = savedMeal.value;
    submitState.value = 'success';
  } catch {
    submitState.value = 'error';
  }
};

onMounted(() => {
  void load();
});
</script>

