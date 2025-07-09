import { Routes, UrlSegment } from '@angular/router';
import { 
  isAuthenticatedGuard,
  isNotAuthenticatedGuard
} from './shared/guards/auth-guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./auth/login/login'),
    canActivate: [isNotAuthenticatedGuard()],
  },
  {
    path: 'register',
    loadComponent: () => import('./auth/register/register'),
    canActivate: [isNotAuthenticatedGuard()],
  },
  {
    path: 'home',
    loadComponent: () => import('./home/home'),
    canActivate: [isAuthenticatedGuard()],
  },
  {
    matcher: (url: UrlSegment[]) => url.length >= 2 && url[0].path.startsWith('@')
      ? ({consumed: url}) : null,
    loadComponent: () => import('./repo/repo').then(m => m.Repo),
  },
  {
    path: '**',
    redirectTo: 'home',
    pathMatch: 'full',
  },
];
