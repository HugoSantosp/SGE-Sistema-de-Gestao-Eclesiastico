import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';
import { ToastService } from '../../services/toast.service';

/**
 * Componente de toast para mensagens de sucesso/erro/info.
 * 
 * Uso:
 * <app-toast></app-toast>
 * 
 * No service:
 * this.toastService.success('Operação realizada com sucesso!');
 * this.toastService.error('Erro ao salvar dados');
 */
@Component({
  selector: 'app-toast',
  template: `
    <div class="toast-container" *ngIf="toasts.length > 0">
      <div *ngFor="let toast of toasts" 
           class="toast-item"
           [class]="'toast-' + toast.type"
           [@slideIn]>
        <div class="toast-icon">
          <i class="bi" [ngClass]="{
            'bi-check-circle-fill': toast.type === 'success',
            'bi-exclamation-circle-fill': toast.type === 'error',
            'bi-info-circle-fill': toast.type === 'info',
            'bi-exclamation-triangle-fill': toast.type === 'warning'
          }"></i>
        </div>
        <div class="toast-content">
          <div class="toast-title" *ngIf="toast.title">{{ toast.title }}</div>
          <div class="toast-message">{{ toast.message }}</div>
        </div>
        <button class="toast-close" (click)="removeToast(toast.id)">
          <i class="bi bi-x"></i>
        </button>
      </div>
    </div>
  `,
  styles: [`
    .toast-container {
      position: fixed;
      top: 20px;
      right: 20px;
      z-index: 9999;
      display: flex;
      flex-direction: column;
      gap: 10px;
      max-width: 400px;
    }

    .toast-item {
      display: flex;
      align-items: flex-start;
      gap: 12px;
      padding: 16px;
      border-radius: 12px;
      background: white;
      box-shadow: 0 10px 40px rgba(0,0,0,0.15);
      animation: slideIn 0.3s ease-out;
      border-left: 4px solid;
    }

    .toast-success {
      border-left-color: var(--success, #22c55e);
    }

    .toast-error {
      border-left-color: var(--danger, #ef4444);
    }

    .toast-warning {
      border-left-color: var(--warning, #f59e0b);
    }

    .toast-info {
      border-left-color: var(--info, #3b82f6);
    }

    .toast-icon {
      font-size: 1.25rem;
      line-height: 1;
    }

    .toast-success .toast-icon { color: var(--success, #22c55e); }
    .toast-error .toast-icon { color: var(--danger, #ef4444); }
    .toast-warning .toast-icon { color: var(--warning, #f59e0b); }
    .toast-info .toast-icon { color: var(--info, #3b82f6); }

    .toast-content {
      flex: 1;
      min-width: 0;
    }

    .toast-title {
      font-weight: 600;
      font-size: 0.875rem;
      color: var(--text-primary, #1a1a2e);
      margin-bottom: 4px;
    }

    .toast-message {
      font-size: 0.8125rem;
      color: var(--text-secondary, #4a5568);
      line-height: 1.4;
    }

    .toast-close {
      background: none;
      border: none;
      color: var(--text-muted, #a0aec0);
      cursor: pointer;
      padding: 4px;
      font-size: 1rem;
      opacity: 0.6;
      transition: opacity 0.2s;
    }

    .toast-close:hover {
      opacity: 1;
    }

    @keyframes slideIn {
      from {
        transform: translateX(100%);
        opacity: 0;
      }
      to {
        transform: translateX(0);
        opacity: 1;
      }
    }
  `]
})
export class ToastComponent implements OnInit, OnDestroy {
  toasts: any[] = [];
  private subscription: Subscription = new Subscription();

  constructor(private toastService: ToastService) {}

  ngOnInit(): void {
    this.subscription.add(
      this.toastService.toasts$.subscribe(toasts => {
        this.toasts = toasts;
      })
    );
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  removeToast(id: number): void {
    this.toastService.remove(id);
  }
}
