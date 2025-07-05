import {
  ChangeDetectionStrategy,
  Component,
  effect,
  inject,
} from '@angular/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Router, RouterModule } from '@angular/router';

import { LoginForm } from './login-form';
import { LoginStore } from './login-store';
import { AuthStore } from '../auth-store';

@Component({
  selector: 'app-login',
  imports: [RouterModule, LoginForm, MatProgressSpinnerModule],
  providers: [LoginStore],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="login">
      @if(authStore.username() === null) {
        <app-login-form
            [loginStatus]="loginStore.userAuthenticated.status()"
            (credentials)="loginStore.credentials$.next($event)"
        />
      } @else {
        <mat-spinner diameter="50"></mat-spinner>
      }
    </div>
  `,
  styles: `
      .login {
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
export default class Login {
  public loginStore = inject(LoginStore);
  public authStore = inject(AuthStore);
  private router = inject(Router);

  constructor() {
    effect(() => {
      if (this.authStore.username()) {
        this.router.navigate(['home']);
      }
    });
  }
}
