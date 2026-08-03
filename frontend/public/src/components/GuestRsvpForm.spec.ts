import { flushPromises, mount } from '@vue/test-utils';
import { afterEach, describe, expect, it, vi } from 'vitest';
import GuestRsvpForm from './GuestRsvpForm.vue';
import { useGuestAccessI18n } from '../i18n/guestAccessI18n';

vi.mock('../services/guestAccessSecuredApi', () => ({
  fetchRsvp: vi.fn(),
  submitRsvp: vi.fn(),
  searchSongs: vi.fn(),
}));

import { fetchRsvp, searchSongs, submitRsvp } from '../services/guestAccessSecuredApi';

const fetchRsvpMock = vi.mocked(fetchRsvp);
const submitRsvpMock = vi.mocked(submitRsvp);
const searchSongsMock = vi.mocked(searchSongs);
const { t } = useGuestAccessI18n();

const laVieEnRose = {
  deezerId: 3135556,
  title: 'La Vie en rose',
  artist: 'Édith Piaf',
  link: 'https://www.deezer.com/track/3135556',
  preview: 'https://cdns-preview.deezer.com/stream/la-vie-en-rose.mp3',
};

const savedRsvp = (
  attendance: 'ATTENDING' | 'DECLINED',
  meal: 'MEAT' | 'FISH' | 'VEGGIE' | null = null,
  song: typeof laVieEnRose | null = null,
) => ({
  id: 'rsvp-1',
  version: 1,
  creationDate: '2026-06-13T10:00:00',
  updateDate: '2026-06-13T10:00:00',
  attendance,
  meal,
  song,
});

const mountForm = () => mount(GuestRsvpForm);

describe('GuestRsvpForm', () => {
  afterEach(() => {
    vi.clearAllMocks();
    vi.useRealTimers();
    vi.unstubAllGlobals();
  });

  it('renders the empty form with a disabled submit when the guest has not responded', async () => {
    fetchRsvpMock.mockResolvedValue(null);
    const wrapper = mountForm();

    await flushPromises();

    expect(wrapper.text()).toContain(t('rsvp.question'));
    expect((wrapper.find('input[value="ATTENDING"]').element as HTMLInputElement).checked).toBe(false);
    expect(wrapper.find('button[type="submit"]').attributes('disabled')).toBeDefined();
  });

  it('prefills the form from an existing response and disables submit until the choice changes', async () => {
    fetchRsvpMock.mockResolvedValue(savedRsvp('ATTENDING', 'VEGGIE'));
    const wrapper = mountForm();

    await flushPromises();

    expect((wrapper.find('input[value="ATTENDING"]').element as HTMLInputElement).checked).toBe(true);
    expect((wrapper.find('input[value="VEGGIE"]').element as HTMLInputElement).checked).toBe(true);
    expect(wrapper.find('button[type="submit"]').attributes('disabled')).toBeDefined();
  });

  it('enables submit when the choice differs from the saved answer and disables it again on return', async () => {
    fetchRsvpMock.mockResolvedValue(savedRsvp('ATTENDING', 'MEAT'));
    const wrapper = mountForm();
    await flushPromises();

    await wrapper.find('input[value="DECLINED"]').trigger('change');
    expect(wrapper.find('button[type="submit"]').attributes('disabled')).toBeUndefined();

    await wrapper.find('input[value="ATTENDING"]').trigger('change');
    expect(wrapper.find('button[type="submit"]').attributes('disabled')).toBeDefined();
  });

  it('hides the meal selector until the guest chooses to attend and hides it again when declining', async () => {
    fetchRsvpMock.mockResolvedValue(null);
    const wrapper = mountForm();
    await flushPromises();

    expect(wrapper.find('input[value="MEAT"]').exists()).toBe(false);

    await wrapper.find('input[value="ATTENDING"]').trigger('change');
    expect(wrapper.text()).toContain(t('rsvp.meal.question'));
    expect(wrapper.find('input[value="MEAT"]').exists()).toBe(true);

    await wrapper.find('input[value="DECLINED"]').trigger('change');
    expect(wrapper.find('input[value="MEAT"]').exists()).toBe(false);
  });

  it('blocks submit for an attending guest until a meal is chosen', async () => {
    fetchRsvpMock.mockResolvedValue(null);
    const wrapper = mountForm();
    await flushPromises();

    await wrapper.find('input[value="ATTENDING"]').trigger('change');
    expect(wrapper.find('button[type="submit"]').attributes('disabled')).toBeDefined();

    await wrapper.find('input[value="FISH"]').trigger('change');
    expect(wrapper.find('button[type="submit"]').attributes('disabled')).toBeUndefined();
  });

  it('submits the selected attendance and meal and shows a success state', async () => {
    fetchRsvpMock.mockResolvedValue(null);
    submitRsvpMock.mockResolvedValue(savedRsvp('ATTENDING', 'MEAT'));
    const wrapper = mountForm();
    await flushPromises();

    await wrapper.find('input[value="ATTENDING"]').trigger('change');
    await wrapper.find('input[value="MEAT"]').trigger('change');
    await wrapper.find('form').trigger('submit');
    await flushPromises();

    expect(submitRsvpMock).toHaveBeenCalledWith('ATTENDING', 'MEAT', null);
    expect(wrapper.text()).toContain(t('rsvp.saved'));
  });

  it('submits a declined answer without a meal', async () => {
    fetchRsvpMock.mockResolvedValue(null);
    submitRsvpMock.mockResolvedValue(savedRsvp('DECLINED'));
    const wrapper = mountForm();
    await flushPromises();

    await wrapper.find('input[value="DECLINED"]').trigger('change');
    await wrapper.find('form').trigger('submit');
    await flushPromises();

    expect(submitRsvpMock).toHaveBeenCalledWith('DECLINED');
    expect(wrapper.text()).toContain(t('rsvp.saved'));
  });

  it('does not fire a duplicate submission when triggered again while one is in flight', async () => {
    fetchRsvpMock.mockResolvedValue(null);
    let resolveSubmit: (value: ReturnType<typeof savedRsvp>) => void = () => {};
    submitRsvpMock.mockReturnValue(
      new Promise((resolve) => {
        resolveSubmit = resolve;
      }),
    );
    const wrapper = mountForm();
    await flushPromises();

    await wrapper.find('input[value="ATTENDING"]').trigger('change');
    await wrapper.find('input[value="MEAT"]').trigger('change');
    await wrapper.find('form').trigger('submit');
    await wrapper.find('form').trigger('submit');

    expect(submitRsvpMock).toHaveBeenCalledTimes(1);

    resolveSubmit(savedRsvp('ATTENDING', 'MEAT'));
    await flushPromises();
  });

  it('surfaces a retriable error when the submission fails', async () => {
    fetchRsvpMock.mockResolvedValue(null);
    submitRsvpMock.mockRejectedValueOnce(new Error('boom'));
    submitRsvpMock.mockResolvedValueOnce(savedRsvp('ATTENDING', 'MEAT'));
    const wrapper = mountForm();
    await flushPromises();

    await wrapper.find('input[value="ATTENDING"]').trigger('change');
    await wrapper.find('input[value="MEAT"]').trigger('change');
    await wrapper.find('form').trigger('submit');
    await flushPromises();

    expect(wrapper.text()).toContain(t('rsvp.submitError'));

    await wrapper.find('form').trigger('submit');
    await flushPromises();

    expect(wrapper.text()).toContain(t('rsvp.saved'));
  });

  it('shows a retriable load error when the RSVP cannot be loaded', async () => {
    fetchRsvpMock.mockRejectedValueOnce(new Error('boom'));
    fetchRsvpMock.mockResolvedValueOnce(null);
    const wrapper = mountForm();
    await flushPromises();

    expect(wrapper.text()).toContain(t('rsvp.loadError'));

    await wrapper.find('button').trigger('click');
    await flushPromises();

    expect(wrapper.text()).toContain(t('rsvp.question'));
  });

  it('shows the song search only when attending', async () => {
    fetchRsvpMock.mockResolvedValue(null);
    const wrapper = mountForm();
    await flushPromises();

    expect(wrapper.find('input[name="song"]').exists()).toBe(false);

    await wrapper.find('input[value="ATTENDING"]').trigger('change');
    expect(wrapper.text()).toContain(t('rsvp.song.label'));
    expect(wrapper.find('input[name="song"]').exists()).toBe(true);

    await wrapper.find('input[value="DECLINED"]').trigger('change');
    expect(wrapper.find('input[name="song"]').exists()).toBe(false);
  });

  it('marks required fields with an asterisk and the song as optional when attending', async () => {
    fetchRsvpMock.mockResolvedValue(null);
    const wrapper = mountForm();
    await flushPromises();

    await wrapper.find('input[value="ATTENDING"]').trigger('change');

    expect(wrapper.findAll('.rsvp-required')).toHaveLength(2);
    const optionalHint = wrapper.find('.rsvp-optional-hint');
    expect(optionalHint.exists()).toBe(true);
    expect(optionalHint.text()).toContain(t('rsvp.optional'));
  });

  it('does not block submitting an attending answer without a song', async () => {
    fetchRsvpMock.mockResolvedValue(null);
    const wrapper = mountForm();
    await flushPromises();

    await wrapper.find('input[value="ATTENDING"]').trigger('change');
    await wrapper.find('input[value="MEAT"]').trigger('change');

    expect(wrapper.find('button[type="submit"]').attributes('disabled')).toBeUndefined();
  });

  it('debounces the Deezer search and lists the suggestions', async () => {
    vi.useFakeTimers();
    fetchRsvpMock.mockResolvedValue(null);
    searchSongsMock.mockResolvedValue([laVieEnRose]);
    const wrapper = mountForm();
    await flushPromises();

    await wrapper.find('input[value="ATTENDING"]').trigger('change');
    await wrapper.find('input[name="song"]').setValue('piaf');

    expect(searchSongsMock).not.toHaveBeenCalled();

    vi.advanceTimersByTime(300);
    await flushPromises();

    expect(searchSongsMock).toHaveBeenCalledWith('piaf');
    expect(wrapper.text()).toContain('La Vie en rose');
    expect(wrapper.text()).toContain('Édith Piaf');
  });

  it('keeps only the three closest suggestions', async () => {
    vi.useFakeTimers();
    fetchRsvpMock.mockResolvedValue(null);
    searchSongsMock.mockResolvedValue(
      Array.from({ length: 8 }, (_, index) => ({ ...laVieEnRose, deezerId: index + 1, title: `Track ${index + 1}` })),
    );
    const wrapper = mountForm();
    await flushPromises();

    await wrapper.find('input[value="ATTENDING"]').trigger('change');
    await wrapper.find('input[name="song"]').setValue('piaf');
    vi.advanceTimersByTime(300);
    await flushPromises();

    expect(wrapper.findAll('ul li')).toHaveLength(3);
  });

  it('ignores a search response that resolves after the query was cleared', async () => {
    vi.useFakeTimers();
    fetchRsvpMock.mockResolvedValue(null);
    let resolveSearch!: (songs: (typeof laVieEnRose)[]) => void;
    searchSongsMock.mockReturnValue(
      new Promise<(typeof laVieEnRose)[]>((resolve) => {
        resolveSearch = resolve;
      }),
    );
    const wrapper = mountForm();
    await flushPromises();

    await wrapper.find('input[value="ATTENDING"]').trigger('change');
    await wrapper.find('input[name="song"]').setValue('piaf');
    vi.advanceTimersByTime(300);
    await flushPromises();

    expect(searchSongsMock).toHaveBeenCalledWith('piaf');

    await wrapper.find('input[name="song"]').setValue('');

    resolveSearch([laVieEnRose]);
    await flushPromises();

    expect(wrapper.findAll('ul li')).toHaveLength(0);
    expect(wrapper.text()).not.toContain('La Vie en rose');
  });

  it('stores a selected suggestion and lets the guest clear it', async () => {
    vi.useFakeTimers();
    fetchRsvpMock.mockResolvedValue(null);
    searchSongsMock.mockResolvedValue([laVieEnRose]);
    const wrapper = mountForm();
    await flushPromises();

    await wrapper.find('input[value="ATTENDING"]').trigger('change');
    await wrapper.find('input[name="song"]').setValue('piaf');
    vi.advanceTimersByTime(300);
    await flushPromises();

    await wrapper.find('ul li button').trigger('click');

    // Selecting replaces the search field with the chosen track and a clear button.
    expect(wrapper.text()).toContain('La Vie en rose');
    expect(wrapper.find('input[name="song"]').exists()).toBe(false);

    const clearButton = wrapper.get(`[aria-label="${t('rsvp.song.clear')}"]`);
    await clearButton.trigger('click');

    // Clearing brings the search field back with no selected track.
    expect(wrapper.find('input[name="song"]').exists()).toBe(true);
  });

  it('toggles a track preview with the play button', async () => {
    vi.useFakeTimers();
    const play = vi
      .spyOn(window.HTMLMediaElement.prototype, 'play')
      .mockImplementation(() => Promise.resolve());
    const pause = vi.spyOn(window.HTMLMediaElement.prototype, 'pause').mockImplementation(() => {});
    fetchRsvpMock.mockResolvedValue(null);
    searchSongsMock.mockResolvedValue([laVieEnRose]);
    const wrapper = mountForm();
    await flushPromises();

    await wrapper.find('input[value="ATTENDING"]').trigger('change');
    await wrapper.find('input[name="song"]').setValue('piaf');
    vi.advanceTimersByTime(300);
    await flushPromises();

    const preview = wrapper.get(`[aria-label="${t('rsvp.song.preview')}"]`);

    await preview.trigger('click');
    expect(play).toHaveBeenCalledTimes(1);
    expect(preview.attributes('aria-pressed')).toBe('true');

    await preview.trigger('click');
    expect(pause).toHaveBeenCalledTimes(1);
    expect(preview.attributes('aria-pressed')).toBe('false');
  });

  it('cancels a pending song search when the guest stops attending', async () => {
    vi.useFakeTimers();
    fetchRsvpMock.mockResolvedValue(null);
    searchSongsMock.mockResolvedValue([laVieEnRose]);
    const wrapper = mountForm();
    await flushPromises();

    await wrapper.find('input[value="ATTENDING"]').trigger('change');
    await wrapper.find('input[name="song"]').setValue('piaf');

    await wrapper.find('input[value="DECLINED"]').trigger('change');
    vi.advanceTimersByTime(300);
    await flushPromises();

    expect(searchSongsMock).not.toHaveBeenCalled();
  });

  it('stops a playing preview when the guest stops attending', async () => {
    vi.useFakeTimers();
    vi.spyOn(window.HTMLMediaElement.prototype, 'play').mockImplementation(() => Promise.resolve());
    const pause = vi.spyOn(window.HTMLMediaElement.prototype, 'pause').mockImplementation(() => {});
    fetchRsvpMock.mockResolvedValue(null);
    searchSongsMock.mockResolvedValue([laVieEnRose]);
    const wrapper = mountForm();
    await flushPromises();

    await wrapper.find('input[value="ATTENDING"]').trigger('change');
    await wrapper.find('input[name="song"]').setValue('piaf');
    vi.advanceTimersByTime(300);
    await flushPromises();

    await wrapper.get(`[aria-label="${t('rsvp.song.preview')}"]`).trigger('click');

    await wrapper.find('input[value="DECLINED"]').trigger('change');

    expect(pause).toHaveBeenCalledTimes(1);
  });

  it('submits the chosen attendance, meal and selected song', async () => {
    vi.useFakeTimers();
    fetchRsvpMock.mockResolvedValue(null);
    searchSongsMock.mockResolvedValue([laVieEnRose]);
    submitRsvpMock.mockResolvedValue(savedRsvp('ATTENDING', 'MEAT', laVieEnRose));
    const wrapper = mountForm();
    await flushPromises();

    await wrapper.find('input[value="ATTENDING"]').trigger('change');
    await wrapper.find('input[value="MEAT"]').trigger('change');
    await wrapper.find('input[name="song"]').setValue('piaf');
    vi.advanceTimersByTime(300);
    await flushPromises();
    await wrapper.find('ul li button').trigger('click');

    vi.useRealTimers();
    await wrapper.find('form').trigger('submit');
    await flushPromises();

    expect(submitRsvpMock).toHaveBeenCalledWith('ATTENDING', 'MEAT', laVieEnRose);
    expect(wrapper.text()).toContain(t('rsvp.saved'));
  });

  it('prefills a previously saved song', async () => {
    fetchRsvpMock.mockResolvedValue(savedRsvp('ATTENDING', 'MEAT', laVieEnRose));
    const wrapper = mountForm();
    await flushPromises();

    expect(wrapper.text()).toContain('La Vie en rose');
    expect(wrapper.find('input[name="song"]').exists()).toBe(false);
    expect(wrapper.find('button[type="submit"]').attributes('disabled')).toBeDefined();
  });

  it('surfaces a recoverable error when the song search fails', async () => {
    vi.useFakeTimers();
    fetchRsvpMock.mockResolvedValue(null);
    searchSongsMock.mockRejectedValue(new Error('boom'));
    const wrapper = mountForm();
    await flushPromises();

    await wrapper.find('input[value="ATTENDING"]').trigger('change');
    await wrapper.find('input[name="song"]').setValue('piaf');
    vi.advanceTimersByTime(300);
    await flushPromises();

    expect(wrapper.text()).toContain(t('rsvp.song.error'));
  });

  it('toggles the preview directly from a previously saved song card', async () => {
    const play = vi
      .spyOn(window.HTMLMediaElement.prototype, 'play')
      .mockImplementation(() => Promise.resolve());
    vi.spyOn(window.HTMLMediaElement.prototype, 'pause').mockImplementation(() => {});
    fetchRsvpMock.mockResolvedValue(savedRsvp('ATTENDING', 'MEAT', laVieEnRose));
    const wrapper = mountForm();
    await flushPromises();

    const preview = wrapper.get(`[aria-label="${t('rsvp.song.preview')}"]`);

    await preview.trigger('click');

    expect(play).toHaveBeenCalledTimes(1);
    expect(preview.attributes('aria-pressed')).toBe('true');
  });

  it('resets the play state when the preview finishes on its own', async () => {
    const audioInstances: HTMLAudioElement[] = [];
    const RealAudio = window.Audio;
    vi.stubGlobal(
      'Audio',
      function AudioMock() {
        const audio = new RealAudio();
        audioInstances.push(audio);
        return audio;
      } as unknown as typeof Audio,
    );
    vi.spyOn(window.HTMLMediaElement.prototype, 'play').mockImplementation(() => Promise.resolve());
    vi.spyOn(window.HTMLMediaElement.prototype, 'pause').mockImplementation(() => {});
    fetchRsvpMock.mockResolvedValue(savedRsvp('ATTENDING', 'MEAT', laVieEnRose));
    const wrapper = mountForm();
    await flushPromises();

    const preview = wrapper.get(`[aria-label="${t('rsvp.song.preview')}"]`);
    await preview.trigger('click');
    expect(preview.attributes('aria-pressed')).toBe('true');

    audioInstances[audioInstances.length - 1].dispatchEvent(new Event('ended'));
    await flushPromises();

    expect(preview.attributes('aria-pressed')).toBe('false');
  });

  it('drops the play state when the preview promise is rejected', async () => {
    vi.spyOn(window.HTMLMediaElement.prototype, 'play').mockImplementation(() => Promise.reject(new Error('blocked')));
    vi.spyOn(window.HTMLMediaElement.prototype, 'pause').mockImplementation(() => {});
    fetchRsvpMock.mockResolvedValue(savedRsvp('ATTENDING', 'MEAT', laVieEnRose));
    const wrapper = mountForm();
    await flushPromises();

    const preview = wrapper.get(`[aria-label="${t('rsvp.song.preview')}"]`);
    await preview.trigger('click');
    await flushPromises();

    expect(preview.attributes('aria-pressed')).toBe('false');
  });

  it('drops the play state when starting the preview throws synchronously', async () => {
    vi.spyOn(window.HTMLMediaElement.prototype, 'play').mockImplementation(() => {
      throw new Error('blocked');
    });
    vi.spyOn(window.HTMLMediaElement.prototype, 'pause').mockImplementation(() => {});
    fetchRsvpMock.mockResolvedValue(savedRsvp('ATTENDING', 'MEAT', laVieEnRose));
    const wrapper = mountForm();
    await flushPromises();

    const preview = wrapper.get(`[aria-label="${t('rsvp.song.preview')}"]`);
    await preview.trigger('click');

    expect(preview.attributes('aria-pressed')).toBe('false');
  });

  it('ignores a stale search whose newer query has already resolved', async () => {
    vi.useFakeTimers();
    fetchRsvpMock.mockResolvedValue(null);
    let resolveStale: (value: typeof laVieEnRose[]) => void = () => {};
    searchSongsMock
      .mockReturnValueOnce(
        new Promise((resolve) => {
          resolveStale = resolve;
        }),
      )
      .mockResolvedValueOnce([laVieEnRose]);
    const wrapper = mountForm();
    await flushPromises();

    await wrapper.find('input[value="ATTENDING"]').trigger('change');
    await wrapper.find('input[name="song"]').setValue('pia');
    vi.advanceTimersByTime(300);
    await wrapper.find('input[name="song"]').setValue('piaf');
    vi.advanceTimersByTime(300);
    await flushPromises();

    expect(wrapper.text()).toContain('La Vie en rose');

    resolveStale([{ ...laVieEnRose, deezerId: 999, title: 'Stale Track' }]);
    await flushPromises();

    expect(wrapper.text()).not.toContain('Stale Track');
  });

  it('ignores a stale search failure once a newer query has resolved', async () => {
    vi.useFakeTimers();
    fetchRsvpMock.mockResolvedValue(null);
    let rejectStale: (reason?: unknown) => void = () => {};
    searchSongsMock
      .mockReturnValueOnce(
        new Promise((_resolve, reject) => {
          rejectStale = reject;
        }),
      )
      .mockResolvedValueOnce([laVieEnRose]);
    const wrapper = mountForm();
    await flushPromises();

    await wrapper.find('input[value="ATTENDING"]').trigger('change');
    await wrapper.find('input[name="song"]').setValue('pia');
    vi.advanceTimersByTime(300);
    await wrapper.find('input[name="song"]').setValue('piaf');
    vi.advanceTimersByTime(300);
    await flushPromises();

    rejectStale(new Error('boom'));
    await flushPromises();

    expect(wrapper.text()).not.toContain(t('rsvp.song.error'));
    expect(wrapper.text()).toContain('La Vie en rose');
  });

  it('restarts the debounce when the query changes before it fires', async () => {
    vi.useFakeTimers();
    fetchRsvpMock.mockResolvedValue(null);
    searchSongsMock.mockResolvedValue([laVieEnRose]);
    const wrapper = mountForm();
    await flushPromises();

    await wrapper.find('input[value="ATTENDING"]').trigger('change');
    await wrapper.find('input[name="song"]').setValue('pi');
    vi.advanceTimersByTime(200);
    await wrapper.find('input[name="song"]').setValue('piaf');
    vi.advanceTimersByTime(300);
    await flushPromises();

    expect(searchSongsMock).toHaveBeenCalledTimes(1);
    expect(searchSongsMock).toHaveBeenCalledWith('piaf');
  });

  it('clears a pending song search when the answer is submitted', async () => {
    vi.useFakeTimers();
    fetchRsvpMock.mockResolvedValue(null);
    searchSongsMock.mockResolvedValue([laVieEnRose]);
    submitRsvpMock.mockResolvedValue(savedRsvp('ATTENDING', 'MEAT'));
    const wrapper = mountForm();
    await flushPromises();

    await wrapper.find('input[value="ATTENDING"]').trigger('change');
    await wrapper.find('input[value="MEAT"]').trigger('change');
    await wrapper.find('input[name="song"]').setValue('piaf');

    vi.useRealTimers();
    await wrapper.find('form').trigger('submit');
    await flushPromises();

    expect(wrapper.text()).toContain(t('rsvp.saved'));
  });

  it('cleans up a pending search and audio preview on unmount', async () => {
    vi.useFakeTimers();
    const pause = vi.spyOn(window.HTMLMediaElement.prototype, 'pause').mockImplementation(() => {});
    vi.spyOn(window.HTMLMediaElement.prototype, 'play').mockImplementation(() => Promise.resolve());
    fetchRsvpMock.mockResolvedValue(null);
    searchSongsMock.mockResolvedValue([laVieEnRose]);
    const wrapper = mountForm();
    await flushPromises();

    await wrapper.find('input[value="ATTENDING"]').trigger('change');
    await wrapper.find('input[name="song"]').setValue('piaf');
    vi.advanceTimersByTime(300);
    await flushPromises();

    await wrapper.get(`[aria-label="${t('rsvp.song.preview')}"]`).trigger('click');
    await wrapper.find('input[name="song"]').setValue('piafx');

    wrapper.unmount();

    expect(pause).toHaveBeenCalled();
  });

  it('ignores a song search that resolves after unmount', async () => {
    vi.useFakeTimers();
    fetchRsvpMock.mockResolvedValue(null);
    let resolveSearch!: (songs: (typeof laVieEnRose)[]) => void;
    searchSongsMock.mockReturnValue(
      new Promise<(typeof laVieEnRose)[]>((resolve) => {
        resolveSearch = resolve;
      }),
    );
    const wrapper = mountForm();
    await flushPromises();

    await wrapper.find('input[value="ATTENDING"]').trigger('change');
    await wrapper.find('input[name="song"]').setValue('piaf');
    vi.advanceTimersByTime(300);
    await flushPromises();

    expect(searchSongsMock).toHaveBeenCalledWith('piaf');

    wrapper.unmount();

    resolveSearch([laVieEnRose]);
    await flushPromises();

    expect(searchSongsMock).toHaveBeenCalledTimes(1);
  });
});


