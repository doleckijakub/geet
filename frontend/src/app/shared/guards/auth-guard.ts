import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthStore } from '../../auth/auth-store';

export const isAuthenticatedGuard = (): CanActivateFn => {
  return () => {
    const authStore = inject(AuthStore);
    const router = inject(Router);

    if (authStore.username()) {
      return true;
    }

    return router.parseUrl('login');
  };
};

export const isNotAuthenticatedGuard = (): CanActivateFn => {
  return () => {
    const authStore = inject(AuthStore);
    const router = inject(Router);

    if (!authStore.username()) {
      return true;
    }

    return router.parseUrl('home');
  };
};
