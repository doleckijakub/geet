import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, tap } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AuthService {
  isLoggedIn = new BehaviorSubject<boolean>(false);
  username = new BehaviorSubject<string | null>(null);

  constructor(private http: HttpClient) {
    this.refreshStatus();
  }

  login(username: string, password: string) {
    return this.http.post('/api/login', { username, password }, {
      withCredentials: true,
      responseType: 'text'
    })
      .pipe(tap(() => this.refreshStatus()));
  }

  logout() {
    return this.http.post('/api/logout', {}, {
      withCredentials: true,
      responseType: 'text'
    })
      .pipe(tap(() => {
        this.isLoggedIn.next(false);
        this.username.next(null);
      }));
  }

  checkStatus() {
    return this.http.get<{ username: string }>('/api/auth-status', {
      withCredentials: true
    })
      .pipe(tap(res => {
        this.isLoggedIn.next(true);
        this.username.next(res.username);
      }, err => {
        this.isLoggedIn.next(false);
        this.username.next(null);
      }));
  }

  refreshStatus() {
    this.http.get<{ username: string }>('/api/auth-status', {
      withCredentials: true
    })
      .subscribe({
        next: res => {
          this.username.next(res.username);
          this.isLoggedIn.next(true);
        },
        error: () => {
          this.username.next(null);
          this.isLoggedIn.next(false);
        }
      });
  }
}
