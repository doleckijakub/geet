import {
  Component,
  input,
  output,
  inject,
  ResourceStatus,
  ChangeDetectionStrategy,
} from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ReactiveFormsModule, FormBuilder } from '@angular/forms';

import { Credentials } from '../interfaces/credentials';

@Component({
  selector: 'app-register-form',
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatProgressSpinnerModule,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <form
      [formGroup]="registerForm"
      (ngSubmit)="credentials.emit(registerForm.getRawValue())"
    >
      <mat-form-field appearance="outline">
        <mat-label>username</mat-label>
        <input
          matNativeControl
          formControlName="username"
          type="text"
          placeholder="username"
        />
        <mat-icon matPrefix>person</mat-icon>
      </mat-form-field>
      <mat-form-field appearance="outline">
        <mat-label>password</mat-label>
        <input
          matNativeControl
          formControlName="password"
          type="password"
          placeholder="password"
        />
        <mat-icon matPrefix>lock</mat-icon>
      </mat-form-field>

      @if (registerStatus()==='error') {
        <mat-error>Could not log you in with those details.</mat-error>
      }
      
      @if (registerStatus() === 'loading') {
        <mat-spinner diameter="50"></mat-spinner>
      }

      <button
        matButton="filled"
        type="submit"
        [disabled]="registerStatus() === 'loading'"
      >
        Register
      </button>
    </form>
  `,
  styles: `
      form {
        display: flex;
        flex-direction: column;
        align-items: center;
      }
      button {
        width: 100%;
      }
      mat-error {
        margin: 5px 0;
      }
      mat-spinner {
        margin: 1rem 0;
      }
    `,
})
export class RegisterForm {
  registerStatus = input.required<ResourceStatus>();
  credentials = output<Credentials>();

  private fb = inject(FormBuilder);

  registerForm = this.fb.nonNullable.group({
    username: [''],
    password: [''],
  });
}
