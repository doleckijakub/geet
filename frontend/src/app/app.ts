import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { AuthStore } from './auth/auth-store';
import { LoadingStore } from './shared/loading-store';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    MatProgressSpinnerModule
  ],
  template: `
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
  .app {
    font-family: sans-serif;
    margin: 2rem;

    form {
      display: flex;
      flex-direction: column;
      max-width: 300px;
      input, button {
        margin-bottom: 0.5rem;
      }
    }

    button {
      cursor: pointer;
    }
  }
  `,
  providers: [
    
  ]
})
export class App {
  protected readonly authStore = inject(AuthStore);
  protected readonly loadingStore = inject(LoadingStore);
}
