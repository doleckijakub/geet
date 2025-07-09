import { Component, Input, Output, EventEmitter } from '@angular/core';
import { RepoData } from './interfaces/repo-data';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-repo-header',
  template: `
  @if (repo) {
    <div class="repo-header">
      <div class="title-section">
        <div class="breadcrumbs">
          <span class="owner">&#64;{{repo.owner}}</span>
          <span class="divider">/</span>
          <span class="repo-name">{{repo.name}}</span>
          <span class="visibility-badge">
            @switch (repo.visibility) {
              @case ('PUBLIC') {
                <mat-icon class="visibility-icon">lock_open</mat-icon>
                Public
              }
              @case ('PRIVATE') {
                <mat-icon class="visibility-icon">lock</mat-icon>
                Private
              }
              @case ('UNLISTED') {
                <mat-icon class="visibility-icon">visibility_off</mat-icon>
                Unlisted
              }
            }
          </span>
        </div>
        
        <div class="action-buttons">
          <button mat-stroked-button class="watch-button">
            <mat-icon>visibility</mat-icon>
            Watch
          </button>
          <button mat-stroked-button class="fork-button">
            <mat-icon>call_split</mat-icon>
            Fork
          </button>
        </div>
      </div>
    </div>
  } @else {
    hihi
  }
  `,
  styles: [`
    @use '../../styles/abstracts/variables' as vars;
    @use '../../styles/abstracts/mixins' as mix;
    @use "sass:color";
    @use "sass:math";

    .repo-header {
      padding: vars.$spacing-unit * 2;
      background-color: vars.$color-header-bg;
      
      .title-section {
        @include mix.flex-center(column, space-between, center);
        gap: vars.$spacing-unit;

        .breadcrumbs {
          @include mix.flex-center;
          font-family: vars.$font-family-sans;
          font-size: vars.$font-size-base;
          color: color.adjust(vars.$color-primary, $lightness: -15%, $space: hsl);

          .owner,
          .repo-name {
            font-weight: 500;
          }

          .divider {
            margin: 0 math.div(vars.$spacing-unit, 2);
          }

          .visibility-badge {
            @include mix.flex-center;
            margin-left: vars.$spacing-unit;
            background: color.adjust(vars.$color-primary, $lightness: 40%, $space: hsl);
            padding: math.div(vars.$spacing-unit, 2) vars.$spacing-unit;
            border-radius: 4px;
            font-size: 0.85em;

            .visibility-icon {
              margin-right: math.div(vars.$spacing-unit, 2);
              font-size: 1em;
              position: relative;
              top: 0.5em;
              left: 0.3em;
            }
          }
        }

        .action-buttons {
          @include mix.flex-center;
          gap: vars.$spacing-unit;

          button {
            font-family: vars.$font-family-sans;

            &.watch-button {
              color: vars.$color-primary;
              border-color: vars.$color-primary;
            }

            &.fork-button {
              color: color.adjust(vars.$color-primary, $lightness: -10%, $space: hsl);
              border-color: color.adjust(vars.$color-primary, $lightness: -10%, $space: hsl);
            }
          }
        }
      }
    }
  `],
  standalone: true,
  imports: [CommonModule, MatIconModule, MatButtonModule, MatTooltipModule]
})
export class RepoHeader {
  @Input() repo: RepoData | null = null;
}