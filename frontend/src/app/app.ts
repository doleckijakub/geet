import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { AuthStore } from './auth/auth-store';
import { LoadingStore } from './shared/data-access/loading-store';
import { Toolbar } from './shell/toolbar';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, MatProgressSpinnerModule, Toolbar],
  template: `
    <app-toolbar></app-toolbar>
    <div class="app">
      @if (loadingStore.isLoading()) {
      <div class="loader">
        <mat-spinner></mat-spinner>
      </div>
      } @else {
      <router-outlet />
      }
    </div>
  `,
  styles: `
    .loader {
      position: fixed;
      z-index: 9999;
      width: 100%;
      height: 30%;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: var(--color-background);
    }
  `,
  providers: [],
})
export class App {
  protected readonly authStore = inject(AuthStore);
  protected readonly loadingStore = inject(LoadingStore);
}
