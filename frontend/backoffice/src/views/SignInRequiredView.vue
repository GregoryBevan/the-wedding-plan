<template>
  <div class="space-y-4 text-center">
    <p>Please sign in with Google to access the backoffice.</p>
    <a
      :href="getGoogleLoginUrl()"
      class="inline-block rounded-md bg-primary px-5 py-2 text-white hover:opacity-90"
    >
      Sign in with Google
    </a>
  </div>
</template>

<script setup lang="ts">
import { getGoogleLoginUrl } from '../services/authApi';
</script>

