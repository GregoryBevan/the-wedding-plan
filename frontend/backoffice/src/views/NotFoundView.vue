<template>
  <div class="space-y-4 text-center">
    <h2 class="text-3xl font-light tracking-wide">Page not found</h2>
    <p class="text-sm text-text/80">The page you are looking for does not exist in the backoffice.</p>
    <RouterLink
      :to="{ name: BACKOFFICE_ROUTE_NAMES.guestList }"
      class="inline-block rounded-md bg-primary px-5 py-2 text-white hover:opacity-90"
    >
      Go to guest list
    </RouterLink>
  </div>
</template>

<script setup lang="ts">
import { RouterLink } from 'vue-router';
import { BACKOFFICE_ROUTE_NAMES } from '../router/routeNames';
</script>

