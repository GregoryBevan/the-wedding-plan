import { createRouter, createWebHistory } from 'vue-router'
import GuestAccessInvitationView from '../views/GuestAccessInvitationView.vue'
import GuestAccessLandingView from '../views/GuestAccessLandingView.vue'
import GuestAccessSecuredAreaView from '../views/GuestAccessSecuredAreaView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'guest-access-home',
      component: GuestAccessLandingView,
    },
    {
      path: '/guest-access/secured-area',
      name: 'guest-access-secured-area',
      component: GuestAccessSecuredAreaView,
    },
    {
      path: '/guest-access/:token',
      name: 'guest-access-invitation',
      component: GuestAccessInvitationView,
      props: true,
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/',
    },
  ],
})

export default router

