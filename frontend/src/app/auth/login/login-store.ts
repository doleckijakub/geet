import { Injectable, inject, signal, computed, effect } from '@angular/core';
import { resource } from '@angular/core';
import { Subject, firstValueFrom } from 'rxjs';
import { toSignal } from '@angular/core/rxjs-interop';

import { AuthStore } from '../auth-store';
import { Credentials } from '../interfaces/credentials';

@Injectable()
export class LoginStore {
  private authStore = inject(AuthStore);

  credentials$ = new Subject<Credentials>();
  credentials = toSignal(this.credentials$);

  readonly userAuthenticated = resource({
    params: this.credentials,
    loader: ({ params }) => {
      if (!params) throw new Error('No credentials');
      return firstValueFrom(this.authStore.login(params));
    }
  });
}
