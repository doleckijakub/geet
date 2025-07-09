import { Component, Input, Output, EventEmitter, inject } from '@angular/core';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';

import { RepoEntry } from './interfaces/repo-entry';
import { Router, ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-repo-file-listing',
  template: `
    <div class="file-listing-container">
      <table mat-table [dataSource]="entries" class="mat-elevation-z1">
        <ng-container matColumnDef="name">
          <mat-header-cell *matHeaderCellDef> Filename </mat-header-cell>
          <mat-cell *matCellDef="let entry" (click)="navigateTo(entry)" style="cursor: pointer;">
            <mat-icon>{{getFileIcon(entry)}}</mat-icon>
            {{entry.name}}
          </mat-cell>
        </ng-container>

        <ng-container matColumnDef="lastCommitShaStr">
          <mat-header-cell *matHeaderCellDef> Commit </mat-header-cell>
          <mat-cell *matCellDef="let entry"> {{entry.lastCommitShaStr}} </mat-cell>
        </ng-container>

        <ng-container matColumnDef="updatedAt">
          <mat-header-cell *matHeaderCellDef> Updated at </mat-header-cell>
          <mat-cell *matCellDef="let entry"> {{howLongAgo(entry.updatedAt)}} </mat-cell>
        </ng-container>

        <mat-header-row *matHeaderRowDef="['name', 'lastCommitShaStr', 'updatedAt']"></mat-header-row>
        <mat-row *matRowDef="let row; columns: ['name', 'lastCommitShaStr', 'updatedAt']"></mat-row>
      </table>
    </div>
  `,
  styles: [`
    @use '../../styles/abstracts/variables' as vars;
    @use "sass:math";
    @use "sass:color";

    .file-listing-container {
      overflow-x: auto;
      margin-top: vars.$spacing-unit * 2;

      table {
        width: 100%;
        border-collapse: collapse;

        th, td {
          padding: vars.$spacing-unit;
          text-align: left;
          border-bottom: 1px solid vars.$color-border;
        }

        th {
          font-weight: 600;
          background-color: vars.$color-header-bg;
        }

        mat-icon {
          vertical-align: middle;
          margin-right: math.div(vars.$spacing-unit, 2);
          color: vars.$color-primary;
        }

        tr {
          cursor: default;

          &:hover {
            background-color: color.adjust(vars.$color-bg, $lightness: 5%, $space: hsl);
          }
        }

        td:first-of-type {
          cursor: pointer;

          &:hover {
            text-decoration: underline;
          }
        }
      }
    }
  `],
  standalone: true,
  imports: [CommonModule, MatTableModule, MatIconModule, MatButtonModule]
})
export class RepoFileListing {
  @Input() entries: RepoEntry[] = [];
  @Input() currentPath: string = '';
  
  router = inject(Router);
  route = inject(ActivatedRoute);

  displayedColumns: string[] = [
    'name',
    'lastCommitShaStr',
    'updatedAt'
  ];

  getFileIcon(entry: RepoEntry): string {
    if (entry.type === 'DIRECTORY') return 'folder';
    return 'insert_drive_file';
  }

  howLongAgo(dateStr: string): string {
    const date = new Date(dateStr);
    const seconds = Math.floor((new Date().getTime() - date.getTime()) / 1000);

    const intervals = [
      { label: "year", seconds: 60 * 60 * 24 * 30 * 12 },
      { label: "month", seconds: 60 * 60 * 24 * 30 },
      { label: "day", seconds: 60 * 60 * 24 },
      { label: "hour", seconds: 60 * 60 },
      { label: "minute", seconds: 60 },
      { label: "second", seconds: 1 },
    ];

    for (const interval of intervals) {
      const count = Math.floor(seconds / interval.seconds);
      if (count >= 1) {
        return `${count} ${interval.label}${count > 1 ? "s" : ""} ago`;
      }
    }

    return "just now";
  }

  navigateTo(entry: RepoEntry): void {
    switch (entry.type) {
      case 'DIRECTORY': {
        this.router.navigate([entry.name], { relativeTo: this.route });
      } break;
      case 'FILE': {
        alert("Previewing files is unimplemented");
      } break;
      case 'SUBMODULE': {
        alert("Redirecting to submodules is unimplemented");
      } break;
      case 'LINK': {
        alert("Redirecting to links is unimplemented");
      } break;
    }
  }
}