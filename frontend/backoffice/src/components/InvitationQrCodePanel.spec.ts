import { mount } from '@vue/test-utils';
import { defineComponent } from 'vue';
import { describe, expect, it, vi } from 'vitest';
import InvitationQrCodePanel from './InvitationQrCodePanel.vue';

const qrCodeStubs = {
  QrcodeCanvas: defineComponent({
    props: {
      value: {
        type: String,
        required: true
      }
    },
    template: '<canvas :data-value="value" data-test="invitation-qr-image"></canvas>'
  }),
  QrcodeSvg: defineComponent({
    props: {
      value: {
        type: String,
        required: true
      }
    },
    template: '<svg :data-value="value" data-test="invitation-qr-svg"></svg>'
  })
};

describe('InvitationQrCodePanel', () => {
  it('shows unavailable state when guest access url is missing', () => {
    const wrapper = mount(InvitationQrCodePanel, {
      props: {
        invitationLabel: 'Family table'
      },
      global: {
        stubs: qrCodeStubs
      }
    });

    expect(wrapper.find('[data-test="invitation-qr-image"]').exists()).toBe(false);
    expect(wrapper.get('[data-test="invitation-qr-unavailable"]').text()).toContain('QR code unavailable for this invitation.');
  });

  it('renders qr preview and guest access url', () => {
    const guestAccessUrl = 'http://localhost:4173/guest-access/token-long-value-12345';

    const wrapper = mount(InvitationQrCodePanel, {
      props: {
        guestAccessUrl,
        invitationLabel: 'Family table'
      },
      global: {
        stubs: qrCodeStubs
      }
    });

    expect(wrapper.get('[data-test="invitation-qr-image"]').element.tagName).toBe('CANVAS');
    expect(wrapper.find('[data-test="invitation-qr-svg"]').exists()).toBe(true);
    expect(wrapper.get('[data-test="invitation-qr-url"]').text()).toContain('token-long-value-12345');
    expect(wrapper.get('[data-test="invitation-qr-url"]').attributes('href')).toBe(guestAccessUrl);
  });

  it('uses custom preview size and falls back to default when invalid', () => {
    const withCustomSize = mount(InvitationQrCodePanel, {
      props: {
        guestAccessUrl: 'http://localhost:4173/guest-access/token-1',
        invitationLabel: 'Family table',
        previewSize: 80
      },
      global: {
        stubs: qrCodeStubs
      }
    });

    const withInvalidSize = mount(InvitationQrCodePanel, {
      props: {
        guestAccessUrl: 'http://localhost:4173/guest-access/token-1',
        invitationLabel: 'Family table',
        previewSize: 0
      },
      global: {
        stubs: qrCodeStubs
      }
    });

    expect(withCustomSize.get('[data-test="invitation-qr-image"]').attributes('style')).toContain('width: 80px;');
    expect(withCustomSize.get('[data-test="invitation-qr-image"]').attributes('style')).toContain('height: 80px;');
    expect(withInvalidSize.get('[data-test="invitation-qr-image"]').attributes('style')).toContain('width: 96px;');
    expect(withInvalidSize.get('[data-test="invitation-qr-image"]').attributes('style')).toContain('height: 96px;');
  });

  it('shows fallback label when token is missing or malformed', () => {
    const missingTokenWrapper = mount(InvitationQrCodePanel, {
      props: {
        guestAccessUrl: 'http://localhost:4173/guest-access/',
        invitationLabel: 'Family table'
      },
      global: {
        stubs: qrCodeStubs
      }
    });

    const malformedTokenWrapper = mount(InvitationQrCodePanel, {
      props: {
        guestAccessUrl: 'http://localhost:4173/guest-access/%E0%A4%A',
        invitationLabel: 'Family table'
      },
      global: {
        stubs: qrCodeStubs
      }
    });

    expect(missingTokenWrapper.get('[data-test="invitation-qr-url"]').text()).toBe('Open invitation link');
    expect(malformedTokenWrapper.get('[data-test="invitation-qr-url"]').text()).toBe('Open invitation link');
  });

  it('downloads png and svg formats', async () => {
    const clickSpy = vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(() => undefined);
    const canvasToDataUrlSpy = vi.spyOn(HTMLCanvasElement.prototype, 'toDataURL').mockReturnValue('data:image/png;base64,qr');

    const createObjectUrlSpy = vi.spyOn(URL, 'createObjectURL').mockReturnValue('blob://qr');
    const revokeObjectUrlSpy = vi.spyOn(URL, 'revokeObjectURL').mockImplementation(() => undefined);

    const wrapper = mount(InvitationQrCodePanel, {
      props: {
        guestAccessUrl: 'http://localhost:4173/guest-access/token-1',
        invitationLabel: 'Family table'
      },
      global: {
        stubs: qrCodeStubs
      }
    });

    await wrapper.get('[data-test="download-qr-png"]').trigger('click');
    await wrapper.get('[data-test="download-qr-svg"]').trigger('click');

    expect(clickSpy).toHaveBeenCalledTimes(2);
    expect(canvasToDataUrlSpy).toHaveBeenCalledWith('image/png');
    expect(createObjectUrlSpy).toHaveBeenCalledTimes(1);
    expect(revokeObjectUrlSpy).toHaveBeenCalledTimes(1);
  });

  it('falls back to preview canvas when export canvas is unavailable for png download', async () => {
    const wrapper = mount(InvitationQrCodePanel, {
      props: {
        guestAccessUrl: 'http://localhost:4173/guest-access/token-1',
        invitationLabel: 'Family table'
      },
      global: {
        stubs: qrCodeStubs
      }
    });

    const canvases = wrapper.findAll('canvas').map((node) => node.element as HTMLCanvasElement);
    const previewCanvas = canvases[0];
    const exportCanvas = canvases[1];

    exportCanvas.remove();

    const previewToDataUrlSpy = vi.fn().mockReturnValue('data:image/png;base64,preview');
    Object.defineProperty(previewCanvas, 'toDataURL', {
      value: previewToDataUrlSpy,
      configurable: true
    });

    await wrapper.get('[data-test="download-qr-png"]').trigger('click');

    expect(previewToDataUrlSpy).toHaveBeenCalledWith('image/png');
  });
});



