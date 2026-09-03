import { Component, EventEmitter, Input, Output } from '@angular/core';

/**
 * Componente de confirmação modal.
 * 
 * Uso:
 * <app-confirm-dialog
 *   [visible]="showConfirmDialog"
 *   title="Confirmar Exclusão"
 *   message="Tem certeza que deseja excluir este registro?"
 *   confirmText="Excluir"
 *   cancelText="Cancelar"
 *   type="danger"
 *   (confirm)="onConfirm()"
 *   (cancel)="onCancel()">
 * </app-confirm-dialog>
 */
@Component({
  selector: 'app-confirm-dialog',
  template: `
    <div class="modal-overlay" *ngIf="visible" (click)="onCancel()">
      <div class="modal-dialog" (click)="$event.stopPropagation()">
        <div class="modal-icon" [ngClass]="'icon-' + type">
          <i class="bi" [ngClass]="{
            'bi-exclamation-triangle-fill': type === 'danger',
            'bi-question-circle-fill': type === 'warning',
            'bi-info-circle-fill': type === 'info',
            'bi-check-circle-fill': type === 'success'
          }"></i>
        </div>
        
        <h3 class="modal-title">{{ title }}</h3>
        <p class="modal-message">{{ message }}</p>
        
        <div class="modal-actions">
          <button class="btn btn-secondary" (click)="onCancel()" [disabled]="loading">
            {{ cancelText }}
          </button>
          <button class="btn" [ngClass]="getConfirmButtonClass()" (click)="onConfirm()" [disabled]="loading">
            <span *ngIf="loading" class="spinner-border spinner-border-sm me-1"></span>
            {{ confirmText }}
          </button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .modal-overlay {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0, 0, 0, 0.5);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 9999;
      animation: fadeIn 0.2s ease-out;
    }

    .modal-dialog {
      background: white;
      border-radius: 16px;
      padding: 32px;
      max-width: 400px;
      width: 90%;
      text-align: center;
      box-shadow: 0 25px 60px rgba(0, 0, 0, 0.2);
      animation: scaleIn 0.2s ease-out;
    }

    .modal-icon {
      width: 64px;
      height: 64px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 0 auto 20px;
      font-size: 2rem;
    }

    .icon-danger {
      background: rgba(239, 68, 68, 0.1);
      color: var(--danger, #ef4444);
    }

    .icon-warning {
      background: rgba(245, 158, 11, 0.1);
      color: var(--warning, #f59e0b);
    }

    .icon-info {
      background: rgba(59, 130, 246, 0.1);
      color: var(--info, #3b82f6);
    }

    .icon-success {
      background: rgba(34, 197, 94, 0.1);
      color: var(--success, #22c55e);
    }

    .modal-title {
      font-size: 1.25rem;
      font-weight: 600;
      color: var(--text-primary, #1a1a2e);
      margin-bottom: 12px;
    }

    .modal-message {
      font-size: 0.9rem;
      color: var(--text-secondary, #4a5568);
      line-height: 1.5;
      margin-bottom: 24px;
    }

    .modal-actions {
      display: flex;
      gap: 12px;
      justify-content: center;
    }

    .modal-actions .btn {
      min-width: 100px;
    }

    @keyframes fadeIn {
      from { opacity: 0; }
      to { opacity: 1; }
    }

    @keyframes scaleIn {
      from {
        transform: scale(0.9);
        opacity: 0;
      }
      to {
        transform: scale(1);
        opacity: 1;
      }
    }
  `]
})
export class ConfirmDialogComponent {
  @Input() visible = false;
  @Input() title = 'Confirmar';
  @Input() message = 'Tem certeza que deseja continuar?';
  @Input() confirmText = 'Confirmar';
  @Input() cancelText = 'Cancelar';
  @Input() type: 'danger' | 'warning' | 'info' | 'success' = 'danger';
  @Input() loading = false;

  @Output() confirm = new EventEmitter<void>();
  @Output() cancel = new EventEmitter<void>();

  onConfirm(): void {
    this.confirm.emit();
  }

  onCancel(): void {
    this.cancel.emit();
  }

  getConfirmButtonClass(): string {
    switch (this.type) {
      case 'danger': return 'btn-danger';
      case 'warning': return 'btn-warning';
      case 'success': return 'btn-success';
      default: return 'btn-primary';
    }
  }
}
