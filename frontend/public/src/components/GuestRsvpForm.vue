<template>
  <section class="mt-3">
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

    <form v-else class="rsvp-form flex min-h-0 flex-1 flex-col gap-3" @submit.prevent="submit">
      <fieldset class="flex flex-col gap-2">
        <legend class="rsvp-legend mb-2 font-semibold text-[#093D57]">
          {{ t('rsvp.question') }}<span class="rsvp-required" :aria-label="t('rsvp.required')">*</span>
        </legend>

        <label
          v-for="choice in choices"
          :key="choice"
          class="rsvp-option flex cursor-pointer items-center gap-3 rounded-xl border border-[#d9c8c2] text-[#093D57]"
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
        <legend class="rsvp-legend mb-2 font-semibold text-[#093D57]">
          {{ t('rsvp.meal.question') }}<span class="rsvp-required" :aria-label="t('rsvp.required')">*</span>
        </legend>

        <label
          v-for="meal in meals"
          :key="meal"
          class="rsvp-option flex cursor-pointer items-center gap-3 rounded-xl border border-[#d9c8c2] text-[#093D57]"
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

      <fieldset v-if="selected === 'ATTENDING'" class="flex flex-col gap-2">
        <legend class="rsvp-legend mb-2 font-semibold text-[#093D57]">
          {{ t('rsvp.song.label') }}<span class="rsvp-optional-hint">({{ t('rsvp.optional') }})</span>
        </legend>

        <div
          v-if="selectedSong"
          class="song-control flex flex-wrap items-center gap-2 rounded-lg border border-[#093D57] bg-[#093D57]/5 text-[#093D57]"
        >
          <div class="flex min-w-0 flex-1 flex-col">
            <span class="truncate font-semibold">{{ selectedSong.title }}</span>
            <span class="truncate text-[#093D57]/60">{{ selectedSong.artist }}</span>
          </div>
          <button
            v-if="selectedSong.preview"
            type="button"
            class="song-icon flex shrink-0 cursor-pointer items-center justify-center rounded-full border border-[#093D57]/40 text-[#093D57]"
            :aria-label="t('rsvp.song.preview')"
            :aria-pressed="playingSongId === selectedSong.deezerId"
            @click="togglePreview(selectedSong)"
          >
            <span aria-hidden="true">{{ playingSongId === selectedSong.deezerId ? '⏸' : '▶' }}</span>
          </button>
          <button
            type="button"
            class="song-icon flex shrink-0 cursor-pointer items-center justify-center rounded-full border border-[#a3352b]/40 text-[#a3352b]"
            :aria-label="t('rsvp.song.clear')"
            :title="t('rsvp.song.clear')"
            @click="clearSong"
          >
            <span aria-hidden="true">✕</span>
          </button>
        </div>

        <template v-else>
          <input
            v-model="songQuery"
            type="search"
            name="song"
            autocomplete="off"
            class="song-control w-full rounded-xl border border-[#d9c8c2] text-[#093D57]"
            :placeholder="t('rsvp.song.placeholder')"
            :aria-label="t('rsvp.song.label')"
          />

          <p v-if="songState === 'searching'" class="song-status text-[#093D57]/70" role="status">
            {{ t('rsvp.song.searching') }}
          </p>
          <p v-else-if="songState === 'error'" class="song-status text-[#a3352b]" role="alert">
            {{ t('rsvp.song.error') }}
          </p>
          <p v-else-if="songState === 'no-results'" class="song-status text-[#093D57]/70" role="status">
            {{ t('rsvp.song.noResults') }}
          </p>

          <ul v-if="songSuggestions.length > 0" class="flex flex-col gap-1">
            <li
              v-for="suggestion in songSuggestions"
              :key="suggestion.deezerId"
              class="song-control flex items-center gap-2 rounded-lg border border-[#d9c8c2] transition-colors hover:border-[#093D57] hover:bg-[#093D57]/5"
            >
              <button
                type="button"
                class="song-select flex min-w-0 flex-1 cursor-pointer items-center gap-2 text-left text-[#093D57]"
                @click="selectSong(suggestion)"
              >
                <span
                  aria-hidden="true"
                  class="song-add flex shrink-0 items-center justify-center rounded-full bg-[#093D57] font-semibold text-white"
                >＋</span>
                <span class="min-w-0 flex-1 truncate font-medium">{{ suggestion.title }}</span>
                <span class="min-w-0 max-w-[40%] truncate text-[#093D57]/60">{{ suggestion.artist }}</span>
              </button>
              <button
                v-if="suggestion.preview"
                type="button"
                class="song-icon flex shrink-0 cursor-pointer items-center justify-center rounded-full border border-[#093D57]/40 text-[#093D57]"
                :aria-label="t('rsvp.song.preview')"
                :aria-pressed="playingSongId === suggestion.deezerId"
                @click="togglePreview(suggestion)"
              >
                <span aria-hidden="true">{{ playingSongId === suggestion.deezerId ? '⏸' : '▶' }}</span>
              </button>
            </li>
          </ul>
        </template>
      </fieldset>

      <div class="rsvp-footer sticky bottom-0 -mx-6 -mb-6 mt-auto bg-white px-6 pb-3 pt-2">
        <button
          class="rsvp-submit w-full rounded-xl bg-[#093D57] font-semibold text-white disabled:opacity-50"
          type="submit"
          :disabled="!canSubmit"
        >
          {{ submitState === 'submitting' ? t('rsvp.submitting') : t('rsvp.submit') }}
        </button>

        <p v-if="submitState === 'success'" class="mt-2 text-sm font-semibold text-[#093D57]" role="status">
          {{ t('rsvp.saved') }}
        </p>
        <p v-else-if="submitState === 'error'" class="mt-2 text-sm text-[#a3352b]" role="alert">
          {{ t('rsvp.submitError') }}
        </p>
      </div>
    </form>
  </section>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { fetchRsvp, searchSongs, submitRsvp, type Meal, type RsvpAttendance, type Song } from '../services/guestAccessSecuredApi';
import { useGuestAccessI18n } from '../i18n/guestAccessI18n';
import type { TranslationKey } from '../i18n/messages/types';

type LoadState = 'loading' | 'ready' | 'error';
type SubmitState = 'idle' | 'submitting' | 'success' | 'error';
type SongState = 'idle' | 'searching' | 'no-results' | 'error';

const SONG_SEARCH_DEBOUNCE_MS = 300;
const SONG_MIN_QUERY_LENGTH = 2;
// Deezer returns results by relevance, so the first few are the closest matches.
const SONG_MAX_SUGGESTIONS = 3;

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

const savedSong = ref<Song | null>(null);
const selectedSong = ref<Song | null>(null);
const songQuery = ref('');
const songSuggestions = ref<Song[]>([]);
const songState = ref<SongState>('idle');
let songSearchTimer: ReturnType<typeof setTimeout> | null = null;
// Guards against a slow earlier search overwriting a newer one's results.
let songSearchSeq = 0;

// A single reusable audio element plays at most one 30s preview at a time.
const playingSongId = ref<number | null>(null);
let previewAudio: HTMLAudioElement | null = null;

// A meal is mandatory to attend, so an attending answer without one can never be submitted.
const mealSatisfied = computed(() => selected.value !== 'ATTENDING' || selectedMeal.value !== null);

// The song is optional, so it only counts as a change (by track identity), never a requirement.
const songChanged = computed(
  () => (selectedSong.value?.deezerId ?? null) !== (savedSong.value?.deezerId ?? null),
);

// A genuine change means a different attendance, or — when attending — a different meal or song.
const changed = computed(() => {
  if (selected.value !== saved.value) {
    return true;
  }

  return selected.value === 'ATTENDING' && (selectedMeal.value !== savedMeal.value || songChanged.value);
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
    savedSong.value = rsvp?.song ?? null;
    selectedSong.value = savedSong.value;
    resetSongSearch();
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

// Cancel a pending debounced call and invalidate any in-flight request: bumping the
// sequence makes a slow response fail its `seq === songSearchSeq` guard, so it can
// never overwrite state the caller is about to reset.
const cancelSongSearch = (): void => {
  if (songSearchTimer !== null) {
    clearTimeout(songSearchTimer);
    songSearchTimer = null;
  }
  songSearchSeq++;
};

const resetSongSearch = (): void => {
  cancelSongSearch();
  stopPreview();
  songQuery.value = '';
  songSuggestions.value = [];
  songState.value = 'idle';
};

const stopPreview = (): void => {
  previewAudio?.pause();
  playingSongId.value = null;
};

// Play/pause a track's 30s preview; a second click on the playing track stops it.
const togglePreview = (song: Song): void => {
  if (!song.preview) {
    return;
  }

  if (playingSongId.value === song.deezerId) {
    stopPreview();
    return;
  }

  if (previewAudio === null) {
    previewAudio = new Audio();
    previewAudio.addEventListener('ended', () => {
      playingSongId.value = null;
    });
  }

  previewAudio.src = song.preview;
  playingSongId.value = song.deezerId;

  try {
    const played = previewAudio.play();
    if (played && typeof played.catch === 'function') {
      played.catch(() => {
        playingSongId.value = null;
      });
    }
  } catch {
    playingSongId.value = null;
  }
};

const select = (attendance: RsvpAttendance): void => {
  selected.value = attendance;
  if (attendance !== 'ATTENDING') {
    resetSongSearch();
  }
  clearOutcome();
};

const selectMeal = (meal: Meal): void => {
  selectedMeal.value = meal;
  clearOutcome();
};

const runSongSearch = async (query: string): Promise<void> => {
  const seq = ++songSearchSeq;

  try {
    const results = await searchSongs(query);
    if (seq !== songSearchSeq) {
      return;
    }
    songSuggestions.value = results.slice(0, SONG_MAX_SUGGESTIONS);
    songState.value = results.length === 0 ? 'no-results' : 'idle';
  } catch {
    if (seq !== songSearchSeq) {
      return;
    }
    songSuggestions.value = [];
    songState.value = 'error';
  }
};

// Debounce the proxy call so we don't fire a request on every keystroke.
watch(songQuery, (query) => {
  cancelSongSearch();

  const trimmed = query.trim();
  if (trimmed.length < SONG_MIN_QUERY_LENGTH) {
    songSuggestions.value = [];
    songState.value = 'idle';
    return;
  }

  songState.value = 'searching';
  songSearchTimer = setTimeout(() => {
    songSearchTimer = null;
    void runSongSearch(trimmed);
  }, SONG_SEARCH_DEBOUNCE_MS);
});

const selectSong = (song: Song): void => {
  selectedSong.value = song;
  resetSongSearch();
  clearOutcome();
};

const clearSong = (): void => {
  selectedSong.value = null;
  resetSongSearch();
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
    // `canSubmit` guarantees a meal is chosen before an attending answer is sent; the song is optional.
    const result =
      selected.value === 'ATTENDING'
        ? await submitRsvp('ATTENDING', selectedMeal.value!, selectedSong.value)
        : await submitRsvp('DECLINED');
    saved.value = result.attendance;
    selected.value = result.attendance;
    savedMeal.value = result.meal ?? null;
    selectedMeal.value = savedMeal.value;
    savedSong.value = result.song ?? null;
    selectedSong.value = savedSong.value;
    resetSongSearch();
    submitState.value = 'success';
  } catch {
    submitState.value = 'error';
  }
};

onMounted(() => {
  void load();
});

onBeforeUnmount(() => {
  // Cancel the debounce and invalidate any in-flight search so a late response can't
  // update refs after the component is gone; also stop a running preview.
  cancelSongSearch();
  previewAudio?.pause();
});
</script>

<style scoped>
/* Keep the form within its card so dynamic content never widens the section.
   All sizing is driven by CSS variables so the device tiers below can retune
   the whole form at once. The base values target older / smaller phones
   (Galaxy S8-S9 ~360px, iPhone SE / 8 ~375px). */
.rsvp-form {
  width: 100%;

  --rsvp-legend-size: 0.85rem;
  --rsvp-text-size: 0.8rem;
  --rsvp-status-size: 0.78rem;
  --rsvp-hint-size: 0.75rem;
  --rsvp-pad-y: 0.45rem;
  --rsvp-pad-x: 0.7rem;
  --rsvp-icon-size: 1.9rem;
  --rsvp-add-size: 1.3rem;
}

/* Modern phones (iPhone 12-15 ~390px, Pixel ~393px, Plus / Pro Max ~428-430px). */
@media (min-width: 390px) {
  .rsvp-form {
    --rsvp-legend-size: 0.95rem;
    --rsvp-text-size: 0.9rem;
    --rsvp-status-size: 0.85rem;
    --rsvp-hint-size: 0.8rem;
    --rsvp-pad-y: 0.55rem;
    --rsvp-pad-x: 0.85rem;
    --rsvp-icon-size: 2.15rem;
    --rsvp-add-size: 1.45rem;
  }
}

/* Tablets and larger. */
@media (min-width: 768px) {
  .rsvp-form {
    --rsvp-legend-size: 1.05rem;
    --rsvp-text-size: 0.95rem;
    --rsvp-status-size: 0.9rem;
    --rsvp-hint-size: 0.85rem;
    --rsvp-pad-y: 0.6rem;
    --rsvp-pad-x: 0.9rem;
    --rsvp-icon-size: 2.4rem;
    --rsvp-add-size: 1.6rem;
  }
}

/* Fieldsets have an implicit `min-inline-size: min-content`, so a long song
   suggestion would otherwise widen the fieldset (and its full-width search
   input) past the card. Pin them so the input stays a fixed size. */
.rsvp-form fieldset {
  min-width: 0;
}

.rsvp-legend {
  margin-bottom: 0.35rem;
  font-size: var(--rsvp-legend-size);
}

/* Required-field asterisk and optional-field hint, kept inline with the legend
   text so they render consistently regardless of the label length. */
.rsvp-required {
  margin-left: 0.15rem;
  color: #a3352b;
  /* The asterisk glyph sits high in its line box, so nudge it down to line up
     with the label text instead of looking like a superscript. */
  position: relative;
  top: 0.18em;
}

.rsvp-optional-hint {
  margin-left: 0.35rem;
  font-size: var(--rsvp-hint-size);
  font-weight: 400;
  color: rgba(9, 61, 87, 0.55);
}

/* Attendance and meal choices. */
.rsvp-option {
  font-size: var(--rsvp-text-size);
  padding: var(--rsvp-pad-y) var(--rsvp-pad-x);
}

/* Submit button stays in scale with the rest of the form. */
.rsvp-submit {
  font-size: var(--rsvp-text-size);
  padding: var(--rsvp-pad-y) 0.9rem;
}

/* Song input, selected-track card and suggestion rows. */
.song-control {
  font-size: var(--rsvp-text-size);
  padding: var(--rsvp-pad-y) var(--rsvp-pad-x);
}

.song-status {
  font-size: var(--rsvp-status-size);
}

/* Round preview/clear buttons that keep their aspect ratio while scaling. */
.song-icon {
  width: var(--rsvp-icon-size);
  height: var(--rsvp-icon-size);
  font-size: var(--rsvp-text-size);
}

/* Filled "＋" badge that signals each suggestion row can be chosen. */
.song-add {
  width: var(--rsvp-add-size);
  height: var(--rsvp-add-size);
  font-size: var(--rsvp-hint-size);
  line-height: 1;
}
</style>

