import { createRouter, createWebHistory } from 'vue-router'
import GuestAccessInvitationView from '../views/GuestAccessInvitationView.vue'
import GuestAccessLandingView from '../views/GuestAccessLandingView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'guest-access-home',
      component: GuestAccessLandingView,
    },
    {
      path: '/guest-access/:token',
      name: 'guest-access-invitation',
      component: GuestAccessInvitationView,
      props: true,
    },
  ],
})

export default router

