import { Routes } from '@angular/router';
import { isAuthenticatedGuard } from './shared/guards/auth-guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./auth/login/login'),
  },
  {
    path: 'register',
    loadComponent: () => import('./auth/register/register'),
  },
  {
    path: 'home',
    loadComponent: () => import('./home/home'),
    canActivate: [isAuthenticatedGuard()],
  },
  {
    path: '**',
    redirectTo: 'home',
    pathMatch: 'full',
  },
];
