import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  input,
  output,
} from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatToolbarModule } from '@angular/material/toolbar';
import { AuthStore } from '../auth/auth-store';
import { Router, RouterLink } from '@angular/router';
import { MatMenuModule } from '@angular/material/menu';
import { take } from 'rxjs';

@Component({
  selector: 'app-toolbar',
  imports: [
    RouterLink,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <mat-toolbar class="toolbar">
      <span>Geet</span>
      <button matIconButton [matMenuTriggerFor]="menu">
        <mat-icon>more_vert</mat-icon>
      </button>
    </mat-toolbar>

    <mat-menu #menu="matMenu">
      <button
        mat-menu-item
        [disabled]="!authStore.username()"
        [routerLink]="'/home'"
      >
        <mat-icon>home</mat-icon>
        <span>Home</span>
      </button>
      @if (authStore.username()) {
      <button mat-menu-item (click)="logout()">
        <mat-icon>logout</mat-icon>
        <span>Logout</span>
      </button>
      } @else {
      <button mat-menu-item [routerLink]="'/login'">
        <mat-icon>login</mat-icon>
        <span>Login</span>
      </button>
      <button mat-menu-item [routerLink]="'/register'">
        <mat-icon>create</mat-icon>
        <span>Register</span>
      </button>
      }
    </mat-menu>
  `,
  styles: `
  .toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }
  `,
})
export class Toolbar {
  private router = inject(Router);
  protected authStore = inject(AuthStore);

  protected logout() {
    this.authStore
      .logout()
      .pipe(take(1))
      .subscribe(() => {
        this.router.navigate(['login']);
      });
  }
}
