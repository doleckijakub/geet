import { Injectable, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, finalize } from 'rxjs';

import { Credentials } from './interfaces/credentials';
import { BasicResponse } from './interfaces/basic-response';
import { ResponseWithUsername } from './interfaces/response-with-username';
import { LoadingStore } from '../shared/loading-store';

@Injectable({
  providedIn: 'root'
})
export class AuthStore {
  username = signal<string | null>(null);

  private loadingStore = inject(LoadingStore);

  constructor(private http: HttpClient) {
    this.loadingStore.showLoader();

    this.getAuthStatus().pipe(
      tap(response => this.username.set(response.username ?? null)),
      finalize(() => this.loadingStore.hideLoader())
    ).subscribe({
      error: () => this.username.set(null)
    });
  }

  register(credentials: Credentials): Observable<BasicResponse> {
    return this.http.post<BasicResponse>('/api/register', credentials);
  }

  login(credentials: Credentials): Observable<ResponseWithUsername> {
    return this.http.post<ResponseWithUsername>('/api/login', credentials).pipe(
      tap(response => this.username.set(response.username ?? null))
    );
  }

  logout(): Observable<BasicResponse> {
    return this.http.post<BasicResponse>('/api/logout', {}).pipe(
      tap(() => this.username.set(null))
    );
  }

  getAuthStatus(): Observable<ResponseWithUsername> {
    return this.http.get<ResponseWithUsername>('/api/auth-status').pipe(
      tap(response => this.username.set(response.username ?? null))
    );
  }
}