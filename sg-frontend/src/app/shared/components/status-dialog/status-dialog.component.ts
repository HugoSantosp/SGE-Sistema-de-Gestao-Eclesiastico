import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';

export interface StatusOption {
  value: string;
  label: string;
  icon?: string;
  hint?: string;
}

/**
 * Diálogo para alterar o status de um registro (contas a pagar/receber).
 *
 * Cuidados que evitam os problemas relatados:
 * - As classes NÃO usam "modal-dialog": o Bootstrap define
 *   `.modal-dialog { pointer-events: none }` globalmente, o que fazia os
 *   cliques atravessarem a caixa e caírem no overlay, fechando o diálogo.
 * - Não fecha ao clicar fora: só em "Cancelar" ou depois de salvar.
 * - As opções são botões grandes com texto e ícone, em vez de um select,
 *   porque o público do sistema não tem familiaridade com componentes nativos.
 */
@Component({
  selector: 'app-status-dialog',
  template: `
    <div class="sge-overlay" *ngIf="visible">
      <div class="sge-modal-box" role="dialog" aria-modal="true">

        <div class="sge-modal-icon">
          <i class="bi bi-arrow-repeat"></i>
        </div>

        <h3 class="sge-modal-title">{{ title }}</h3>
        <p class="sge-modal-message" *ngIf="registro">{{ registro }}</p>

        <div class="opcoes">
          <button type="button"
                  *ngFor="let opt of options"
                  class="opcao"
                  [class.selecionada]="selecionado === opt.value"
                  [disabled]="loading"
                  (click)="selecionar(opt.value)">
            <i class="bi" [ngClass]="opt.icon || 'bi-circle'"></i>
            <span class="opcao-texto">
              <span class="opcao-label">{{ opt.label }}</span>
              <span class="opcao-hint" *ngIf="opt.hint">{{ opt.hint }}</span>
            </span>
            <i class="bi bi-check-circle-fill check" *ngIf="selecionado === opt.value"></i>
          </button>
        </div>

        <p class="aviso" *ngIf="selecionado === currentStatus">
          Este já é o status atual da conta.
        </p>

        <div class="sge-modal-actions">
          <button type="button" class="btn btn-secondary" (click)="onCancel()" [disabled]="loading">
            Cancelar
          </button>
          <button type="button" class="btn btn-primary" (click)="onConfirm()"
                  [disabled]="loading || !selecionado || selecionado === currentStatus">
            <span *ngIf="loading" class="spinner-border spinner-border-sm me-1"></span>
            Salvar alteração
          </button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .sge-overlay {
      position: fixed;
      top: 0; left: 0; right: 0; bottom: 0;
      background: rgba(0, 0, 0, 0.55);
      display: flex; align-items: center; justify-content: center;
      z-index: 9999;
      padding: 16px;
      animation: fadeIn 0.15s ease-out;
    }
    .sge-modal-box {
      pointer-events: auto;
      background: var(--bg-elevated, #fff);
      border-radius: 16px;
      padding: 26px;
      width: 100%;
      max-width: 440px;
      max-height: 90vh;
      overflow-y: auto;
      box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
      animation: slideUp 0.2s ease-out;
    }
    .sge-modal-icon {
      width: 52px; height: 52px;
      border-radius: 50%;
      display: flex; align-items: center; justify-content: center;
      font-size: 1.4rem;
      margin: 0 auto 14px;
      background: rgba(59, 130, 246, 0.12);
      color: #2563eb;
    }
    .sge-modal-title { font-size: 1.15rem; font-weight: 700; text-align: center; margin-bottom: 6px; }
    .sge-modal-message { font-size: 0.9rem; color: var(--text-secondary, #6b7280); text-align: center; margin-bottom: 18px; }

    .opcoes { display: flex; flex-direction: column; gap: 10px; margin-bottom: 14px; }
    .opcao {
      display: flex; align-items: center; gap: 12px;
      width: 100%;
      padding: 14px 16px;
      border-radius: 12px;
      border: 2px solid var(--border-light, #e5e7eb);
      background: transparent;
      color: var(--text-primary, #111827);
      font-size: 0.95rem;
      text-align: left;
      cursor: pointer;
      transition: border-color 0.15s, background 0.15s;
    }
    .opcao:hover:not(:disabled) { border-color: #f97316; background: rgba(249, 115, 22, 0.06); }
    .opcao.selecionada { border-color: #f97316; background: rgba(249, 115, 22, 0.1); font-weight: 600; }
    .opcao:disabled { opacity: 0.6; cursor: not-allowed; }
    .opcao > .bi:first-child { font-size: 1.25rem; color: #f97316; }
    .opcao-texto { display: flex; flex-direction: column; flex: 1; }
    .opcao-label { font-weight: 600; }
    .opcao-hint { font-size: 0.78rem; color: var(--text-secondary, #6b7280); font-weight: 400; }
    .check { color: #f97316; font-size: 1.1rem; }

    .aviso {
      font-size: 0.8rem;
      color: var(--text-secondary, #6b7280);
      text-align: center;
      margin-bottom: 12px;
    }
    .sge-modal-actions { display: flex; gap: 10px; justify-content: flex-end; }
    @keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
    @keyframes slideUp { from { transform: translateY(14px); opacity: 0; } to { transform: translateY(0); opacity: 1; } }
  `]
})
export class StatusDialogComponent implements OnChanges {
  @Input() visible = false;
  @Input() title = 'Alterar status';
  @Input() registro = '';
  @Input() currentStatus = '';
  @Input() options: StatusOption[] = [];
  @Input() loading = false;

  @Output() confirm = new EventEmitter<string>();
  @Output() cancel = new EventEmitter<void>();

  selecionado = '';

  ngOnChanges(changes: SimpleChanges): void {
    // Só reinicia a seleção quando o diálogo ABRE.
    // Antes, qualquer mudança de input (inclusive um array recriado a cada
    // ciclo de detecção) apagava a escolha do usuário.
    const abriu = changes['visible']
      && changes['visible'].currentValue === true
      && changes['visible'].previousValue !== true;
    if (abriu) {
      this.selecionado = this.currentStatus || '';
    }
  }

  selecionar(valor: string): void {
    if (this.loading) return;
    this.selecionado = valor;
  }

  onConfirm(): void {
    if (this.selecionado && this.selecionado !== this.currentStatus) {
      this.confirm.emit(this.selecionado);
    }
  }

  onCancel(): void {
    if (!this.loading) this.cancel.emit();
  }
}
