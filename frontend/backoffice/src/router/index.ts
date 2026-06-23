import { createRouter, createWebHistory } from 'vue-router';
import type { RouteRecordRaw, RouterHistory } from 'vue-router';
import { getAuthStatus } from '../services/authApi';
import { BACKOFFICE_ROUTE_NAMES } from './routeNames';

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: { name: BACKOFFICE_ROUTE_NAMES.guestList }
  },
  {
    path: '/guests',
    name: BACKOFFICE_ROUTE_NAMES.guestList,
    component: () => import('../views/GuestListView.vue'),
    meta: {
      requiresAuthorized: true
    }
  },
  {
    path: '/guests/new',
    name: BACKOFFICE_ROUTE_NAMES.guestAdd,
    component: () => import('../views/AddGuestView.vue'),
    meta: {
      requiresAuthorized: true
    }
  },
  {
    path: '/login-required',
    name: BACKOFFICE_ROUTE_NAMES.signInRequired,
    component: () => import('../views/SignInRequiredView.vue')
  },
  {
    path: '/access-denied',
    name: BACKOFFICE_ROUTE_NAMES.accessDenied,
    component: () => import('../views/AccessDeniedView.vue')
  },
  {
    path: '/:pathMatch(.*)*',
    name: BACKOFFICE_ROUTE_NAMES.notFound,
    component: () => import('../views/NotFoundView.vue')
  }
];

export const createBackofficeRouter = (
  history: RouterHistory = createWebHistory(import.meta.env.BASE_URL)
) => {
  const router = createRouter({
    history,
    routes
  });

  router.beforeEach(async (to) => {
    const requiresAuthorized = to.matched.some((record) => record.meta.requiresAuthorized);

    if (!requiresAuthorized) {
      return true;
    }

    try {
      const status = await getAuthStatus();

      if (!status.isAuthenticated) {
        return { name: BACKOFFICE_ROUTE_NAMES.signInRequired };
      }

      if (!status.isAuthorized) {
        return { name: BACKOFFICE_ROUTE_NAMES.accessDenied };
      }

      return true;
    } catch {
      return { name: BACKOFFICE_ROUTE_NAMES.signInRequired };
    }
  });

  return router;
};

export default createBackofficeRouter();


