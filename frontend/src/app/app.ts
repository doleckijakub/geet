import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { AuthService } from './auth.service';

@Component({
selector: 'app-root',
standalone: true,
imports: [
CommonModule,
HttpClientModule,  // ✅ Add this
],
templateUrl: './app.html',
styleUrl: './app.scss',
providers: [AuthService]
})
export class AppComponent {
  auth = inject(AuthService);

  login(event: Event) {
    event.preventDefault();
    const form = event.target as HTMLFormElement;
    const username = (form['username'] as HTMLInputElement).value;
    const password = (form['password'] as HTMLInputElement).value;
    this.auth.login(username, password).subscribe({
      next: () => {
        this.auth.refreshStatus();
      },
      error: (err) => {
        console.error('Login failed:', err);
      }
    });
  }

  logout() {
    this.auth.logout().subscribe();
  }
}
