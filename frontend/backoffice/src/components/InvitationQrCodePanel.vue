<template>
  <section class="space-y-3 overflow-hidden rounded-xl border border-secondary/30 bg-white/70 p-4" data-test="invitation-qr-panel">
    <header class="flex items-center justify-between gap-3">
      <h4 class="text-sm font-medium text-text">Invitation QR code</h4>
      <div class="flex items-center gap-2">
        <button
          :disabled="!guestAccessUrl"
          class="inline-flex rounded border border-secondary px-2 py-1 text-xs text-text transition hover:bg-secondary/20 disabled:cursor-not-allowed disabled:opacity-60"
          data-test="download-qr-png"
          type="button"
          @click="downloadPng"
        >
          PNG
        </button>
        <button
          :disabled="!guestAccessUrl"
          class="inline-flex rounded border border-secondary px-2 py-1 text-xs text-text transition hover:bg-secondary/20 disabled:cursor-not-allowed disabled:opacity-60"
          data-test="download-qr-svg"
          type="button"
          @click="downloadSvg"
        >
          SVG
        </button>
      </div>
    </header>

    <div ref="qrPreviewContainer" class="flex w-full items-center justify-center rounded border border-secondary/20 bg-white p-3">
      <QrcodeCanvas
        v-if="guestAccessUrl"
        :value="guestAccessUrl"
        :size="previewSize"
        :margin="1"
        foreground="#37474f"
        level="M"
        :style="qrPreviewStyle"
        data-test="invitation-qr-image"
      />
      <p v-else class="text-xs text-text/60" data-test="invitation-qr-preview-unavailable">QR code preview unavailable.</p>
    </div>

    <div ref="qrSvgContainer" class="hidden" aria-hidden="true">
      <QrcodeCanvas
        v-if="guestAccessUrl"
        :value="guestAccessUrl"
        :size="384"
        :margin="1"
        foreground="#37474f"
        level="M"
        data-test="invitation-qr-export-canvas"
      />

      <QrcodeSvg
        v-if="guestAccessUrl"
        :value="guestAccessUrl"
        :size="280"
        :margin="1"
        foreground="#37474f"
        level="M"
        data-test="invitation-qr-svg"
      />
    </div>

    <a
      v-if="guestAccessUrl"
      :href="guestAccessUrl"
      :title="guestAccessUrl"
      class="block w-full break-all text-center text-xs text-primary underline"
      data-test="invitation-qr-url"
      rel="noopener noreferrer"
      target="_blank"
    >
      {{ compactGuestAccessLabel }}
    </a>
    <p v-else class="text-xs text-text/60" data-test="invitation-qr-unavailable">QR code unavailable for this invitation.</p>
  </section>
</template>

<script setup lang="ts">
import { QrcodeCanvas, QrcodeSvg } from 'qrcode.vue';
import { computed, ref } from 'vue';

const props = defineProps<{
  guestAccessUrl?: string;
  invitationLabel: string;
  previewSize?: number;
}>();

const qrPreviewContainer = ref<HTMLElement | null>(null);
const qrSvgContainer = ref<HTMLElement | null>(null);

const previewSize = computed(() => {
  if (!props.previewSize || props.previewSize <= 0) {
    return 96;
  }

  return props.previewSize;
});

const qrPreviewStyle = computed(() => ({
  width: `${previewSize.value}px`,
  height: `${previewSize.value}px`
}));

const compactGuestAccessLabel = computed(() => {
  if (!props.guestAccessUrl) {
    return '';
  }

  try {
    const token = decodeURIComponent(props.guestAccessUrl.split('/').pop() ?? '').trim();
    if (!token) {
      return 'Open invitation link';
    }

    return token;
  } catch {
    return 'Open invitation link';
  }
});

const buildDownloadFilename = (extension: 'png' | 'svg') => {
  const baseLabel = props.invitationLabel.trim().toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/(^-|-$)/g, '') || 'invitation';
  return `${baseLabel}-qr.${extension}`;
};

const triggerDownload = (href: string, filename: string) => {
  const link = document.createElement('a');
  link.href = href;
  link.download = filename;
  link.click();
};

const resolveQrCanvasElement = (): HTMLCanvasElement | undefined => {
  return qrPreviewContainer.value?.querySelector('canvas') ?? undefined;
};

const resolveQrExportCanvasElement = (): HTMLCanvasElement | undefined => {
  return qrSvgContainer.value?.querySelector('[data-test="invitation-qr-export-canvas"]') as HTMLCanvasElement | undefined;
};

const resolveQrSvgElement = (): SVGSVGElement | undefined => {
  return qrSvgContainer.value?.querySelector('svg') ?? undefined;
};

const downloadPng = () => {
  const canvas = resolveQrExportCanvasElement() ?? resolveQrCanvasElement();

  if (!canvas || typeof canvas.toDataURL !== 'function') {
    return;
  }

  triggerDownload(canvas.toDataURL('image/png'), buildDownloadFilename('png'));
};

const downloadSvg = () => {
  const svgElement = resolveQrSvgElement();

  if (!svgElement) {
    return;
  }

  const svgMarkup = new XMLSerializer().serializeToString(svgElement);

  const blob = new Blob([svgMarkup], { type: 'image/svg+xml' });
  const objectUrl = URL.createObjectURL(blob);

  try {
    triggerDownload(objectUrl, buildDownloadFilename('svg'));
  } finally {
    URL.revokeObjectURL(objectUrl);
  }
};
</script>


