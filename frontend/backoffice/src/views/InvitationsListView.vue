<template>
  <section>
    <header class="mb-6 text-center">
      <h2 class="text-3xl font-light tracking-wide text-text">Invitations</h2>
    </header>

    <div class="grid gap-4 md:grid-cols-2" data-test="invitation-card-list">
      <article
        v-for="invitation in invitationPreviewCards"
        :key="invitation.id"
        class="rounded-xl border border-secondary/30 bg-white p-4 shadow-sm"
        data-test="invitation-card"
      >
        <h3 class="mb-3 text-lg font-medium text-text">{{ invitation.label }}</h3>

        <p class="mb-2 text-sm text-text/80">{{ invitation.description }}</p>
        <p class="text-xs text-text/60">{{ invitation.guestCount }} guests</p>
      </article>
    </div>
  </section>
</template>

<script setup lang="ts">
const invitationPreviewCards = [
  {
    id: 'family-table',
    label: 'Family table',
    description: 'Main family invitation for the front row table.',
    guestCount: 8
  },
  {
    id: 'friends-brunch',
    label: 'Friends brunch',
    description: 'Brunch invitation for close friends the day after.',
    guestCount: 14
  }
];
</script>

