<template>
  <nav aria-label="Backoffice navigation">
    <ul class="space-y-2">
      <li>
        <RouterLink
          :aria-current="ariaCurrent('/invitations')"
          :class="linkClasses('/invitations')"
          to="/invitations"
        >
          <span
            aria-hidden="true"
            class="sidebar-link-icon inline-flex h-5 w-5 shrink-0 [&>svg]:h-5 [&>svg]:w-5"
            v-html="menuInvitationIcon"
          ></span>
          <span>Invitations</span>
        </RouterLink>
      </li>
      <li>
        <RouterLink
          :aria-current="ariaCurrent('/guests')"
          :class="linkClasses('/guests')"
          to="/guests"
        >
          <span
            aria-hidden="true"
            class="sidebar-link-icon inline-flex h-5 w-5 shrink-0 [&>svg]:h-5 [&>svg]:w-5"
            v-html="menuGuestIcon"
          ></span>
          <span>Guests</span>
        </RouterLink>
      </li>
    </ul>
  </nav>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router';
import menuGuestIcon from '../assets/icons/menu-guests.svg?raw';
import menuInvitationIcon from '../assets/icons/menu-invitations.svg?raw';

const route = useRoute();
const baseLinkClasses = 'flex items-center gap-2 rounded-md px-3 py-2 transition-colors';
const activeLinkClasses = 'bg-primary text-white';
const inactiveLinkClasses = 'text-text/90 hover:bg-background';

const isRouteActive = (linkPath: string) => route.path === linkPath || route.path.startsWith(`${linkPath}/`);

const linkClasses = (linkPath: string) => [
  baseLinkClasses,
  isRouteActive(linkPath) ? activeLinkClasses : inactiveLinkClasses
];

const ariaCurrent = (linkPath: string) => isRouteActive(linkPath) ? 'page' : undefined;
</script>

