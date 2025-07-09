import { Component, OnInit, OnDestroy, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Observable, Subscription, map } from 'rxjs';
import { MatCard, MatCardContent, MatCardHeader, MatCardTitle } from '@angular/material/card';
import { MatDivider } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { RepoData } from './interfaces/repo-data';
import { RepoHeader } from './repo-header';
import { RepoFileListing } from './repo-file-listing';

@Component({
  selector: 'app-repo',
  template: `
    @if (error()) {
    <mat-card class="error-card">
      <mat-card-header>
        <mat-card-title>Error loading repository</mat-card-title>
      </mat-card-header>
      <mat-card-content>
        <p>{{error()}}</p>
        <button mat-raised-button color="primary" (click)="loadRepository()">Retry</button>
      </mat-card-content>
    </mat-card>
    } @else if(repoData()) {
    <div class="repo-container">
      <div class="repo-content">
        <app-repo-header [repo]="repoData()"></app-repo-header>

        <mat-divider></mat-divider>

        <app-repo-file-listing 
          [entries]="repoData()?.entries || []"
          [currentPath]="''">
        </app-repo-file-listing>
      </div>
    </div>
    } @else {
      <mat-spinner diameter="50"></mat-spinner>
    }
  `,
  styles: [`
    @use '../../styles/abstracts/variables' as vars;
    @use '../../styles/abstracts/mixins' as mix;

    .repo-container {
      padding: vars.$spacing-unit * 3;
      background-color: vars.$color-bg;
    }

    .repo-content {
      @include mix.flex-center(column, flex-start, stretch);
      gap: vars.$spacing-unit * 3;

      mat-card.error-card {
        border: 1px solid red;
      }

      mat-spinner {
        margin: auto;
      }
    }
  `],
  imports: [
    RepoHeader,
    RepoFileListing,

    MatCard,
    MatCardHeader,
    MatCardTitle,
    MatCardContent,
    MatDivider,
    MatProgressSpinnerModule
  ],
})
export class Repo implements OnInit, OnDestroy {
  private http = inject(HttpClient);
  private router = inject(Router);

  private routeSubscription!: Subscription;
  
  username = signal<string>('');
  repoName = signal<string>('');
  branch = signal<string>('');
  path = signal<string>('');

  repoData = signal<RepoData | null>(null);
  loading = signal<boolean>(true);
  error = signal<string | null>(null);

  constructor(
    private route: ActivatedRoute,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.routeSubscription = this.route.url.subscribe(segments => {
      const username = segments[0]?.path.substring(1);
      const repoName = segments[1]?.path;
      const branch = segments[2]?.path;
      const path = segments.slice(3).map(s => s.path).join('/');

      this.username.set(username);
      this.repoName.set(repoName);
      this.branch.set(branch);
      this.path.set(path);

      this.loadRepository();
    });
  }

  ngOnDestroy(): void {
    if (this.routeSubscription) this.routeSubscription.unsubscribe();
  }

  loadRepository(): void {
    this.loading.set(true);
    this.error.set(null);
    
    this.getRepository(this.username(), this.repoName(), this.branch(), this.path()).subscribe({
      next: (data) => {
        this.repoData.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(`Failed to load repository`);
        this.loading.set(false);
        this.snackBar.open(this.error() ?? '', 'Dismiss', { duration: 5000 });
      }
    });
  }

  getRepository(
    username: string,
    repoName: string,
    branch: string | null,
    path: string | null
  ): Observable<RepoData> {
    let url = `/api/repo/${username}/${repoName}`;
    if (branch) url += `/${branch}`;
    if (path) url += `/${path}`;

    return this.http
      .get<RepoData | { error: string }>(url, {})
      .pipe(
        map((response) => {
          if ('entries' in response) {
            if (branch == null) {
              this.router.navigate([response.defaultBranch], { relativeTo: this.route });
            }

            return response as RepoData;
          } else if ('error' in response) {
            throw new Error(response.error);
          } else {
            throw new Error("Invalid response");
          }
        })
      );
}
}