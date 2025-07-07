import { inject, Injectable, signal } from '@angular/core';
import { NavigationCancel, NavigationEnd, NavigationError, NavigationStart, Router } from '@angular/router';

@Injectable({
  providedIn: 'root',
})
export class LoadingStore {
 readonly isLoading = signal(false);

  constructor() {
    const router = inject(Router);
    router.events.subscribe((event) => {
      if (event instanceof NavigationStart) {
        this.showLoader();
      } else if (
        event instanceof NavigationEnd ||
        event instanceof NavigationCancel ||
        event instanceof NavigationError
      ) {
        this.hideLoader();
      }
    });
  }

  showLoader() {
    this.isLoading.set(true);
  }

  hideLoader() {
    this.isLoading.set(false);
  }
}
