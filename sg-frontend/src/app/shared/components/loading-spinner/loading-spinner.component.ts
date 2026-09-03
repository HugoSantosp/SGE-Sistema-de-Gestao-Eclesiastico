import { Component, Input } from '@angular/core';

/**
 * Componente de loading spinner reutilizável.
 * 
 * Uso:
 * <app-loading-spinner [loading]="isLoading" [size]="'lg'" [text]="'Carregando...'">
 *   <!-- Conteúdo que será exibido quando não estiver carregando -->
 * </app-loading-spinner>
 * 
 * Ou sem conteúdo (overlay):
 * <app-loading-spinner [loading]="isLoading" [overlay]="true"></app-loading-spinner>
 */
@Component({
  selector: 'app-loading-spinner',
  template: `
    <div class="spinner-wrapper" *ngIf="loading" [class.overlay]="overlay">
      <div class="spinner-content">
        <div class="spinner" [ngClass]="'spinner-' + size">
          <div class="spinner-ring"></div>
        </div>
        <div class="spinner-text" *ngIf="text">{{ text }}</div>
      </div>
    </div>
    <ng-content *ngIf="!loading"></ng-content>
  `,
  styles: [`
    .spinner-wrapper {
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 40px 20px;
    }

    .spinner-wrapper.overlay {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(255, 255, 255, 0.9);
      z-index: 100;
      padding: 0;
    }

    .spinner-content {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 16px;
    }

    .spinner {
      position: relative;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .spinner-ring {
      border-radius: 50%;
      border-style: solid;
      border-color: var(--border-color, #e2e8f0);
      border-top-color: var(--orange, #f97316);
      animation: spin 0.8s linear infinite;
    }

    /* Tamanhos */
    .spinner-sm .spinner-ring {
      width: 24px;
      height: 24px;
      border-width: 2px;
    }

    .spinner-md .spinner-ring {
      width: 40px;
      height: 40px;
      border-width: 3px;
    }

    .spinner-lg .spinner-ring {
      width: 56px;
      height: 56px;
      border-width: 4px;
    }

    .spinner-xl .spinner-ring {
      width: 72px;
      height: 72px;
      border-width: 5px;
    }

    .spinner-text {
      font-size: 0.875rem;
      color: var(--text-secondary, #4a5568);
      font-weight: 500;
    }

    @keyframes spin {
      to {
        transform: rotate(360deg);
      }
    }
  `]
})
export class LoadingSpinnerComponent {
  @Input() loading = false;
  @Input() size: 'sm' | 'md' | 'lg' | 'xl' = 'md';
  @Input() text = '';
  @Input() overlay = false;
}
