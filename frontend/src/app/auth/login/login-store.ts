import { Injectable, inject, signal, computed, effect } from '@angular/core';
import { resource } from '@angular/core';
import { Subject, firstValueFrom } from 'rxjs';
import { toSignal } from '@angular/core/rxjs-interop';

import { AuthStore } from '../auth-store';
import { Credentials } from '../interfaces/credentials';
import { LoadingStore } from '../../shared/data-access/loading-store';

@Injectable()
export class LoginStore {
  private authStore = inject(AuthStore);
  private loadingStore = inject(LoadingStore);

  credentials$ = new Subject<Credentials>();
  credentials = toSignal(this.credentials$);

  readonly userAuthenticated = resource({
    params: this.credentials,
    loader: async ({ params }) => {
      if (!params) throw new Error('No credentials');

      this.loadingStore.showLoader();
      try {
        const result = await firstValueFrom(this.authStore.login(params));
        return result;
      } finally {
        this.loadingStore.hideLoader();
      }
    },
  });
}
