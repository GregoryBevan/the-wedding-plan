<template>
  <slot v-if="canWrite" />
</template>

<script setup lang="ts">
import { useCapabilities } from '../../composables/useCapabilities';

defineOptions({
  name: 'WriteOnly'
});

const { canWrite } = useCapabilities();
</script>

