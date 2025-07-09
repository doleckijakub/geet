import {
  ChangeDetectionStrategy,
  Component,
  effect,
  inject,
} from '@angular/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Router, RouterModule } from '@angular/router';

import { RegisterForm } from './register-form';
import { RegisterStore } from './register-store';
import { AuthStore } from '../auth-store';

@Component({
  selector: 'app-register',
  imports: [RouterModule, RegisterForm, MatProgressSpinnerModule],
  providers: [RegisterStore],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="register">
      @if(authStore.username() === null) {
        <app-register-form
            [registerStatus]="registerStore.userAuthenticated.status()"
            (credentials)="registerStore.credentials$.next($event)"
        />
      } @else {
        <mat-spinner diameter="50"></mat-spinner>
      }
    </div>
  `,
  styles: `
      .register {
        height: 100%;
        display: flex;
        justify-content: center;
        margin-top: 5rem;
        a {
          margin: 2rem;
          color: var(--accent-darker-color);
        }
      }
    `,
})
export default class Register {
  public registerStore = inject(RegisterStore);
  public authStore = inject(AuthStore);
  private router = inject(Router);
}
