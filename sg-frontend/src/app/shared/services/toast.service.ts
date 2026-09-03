import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export interface Toast {
  id: number;
  type: 'success' | 'error' | 'warning' | 'info';
  title?: string;
  message: string;
  duration?: number;
}

/**
 * Service para gerenciar notificações toast.
 * 
 * Uso:
 * this.toastService.success('Sucesso!', 'Operação realizada com sucesso');
 * this.toastService.error('Erro', 'Não foi possível salvar');
 */
@Injectable({ providedIn: 'root' })
export class ToastService {
  private toastsSubject = new BehaviorSubject<Toast[]>([]);
  public toasts$ = this.toastsSubject.asObservable();

  private counter = 0;

  /**
   * Exibe toast de sucesso
   */
  success(message: string, title?: string, duration: number = 5000): void {
    this.show('success', message, title, duration);
  }

  /**
   * Exibe toast de erro
   */
  error(message: string, title?: string, duration: number = 8000): void {
    this.show('error', message, title, duration);
  }

  /**
   * Exibe toast de aviso
   */
  warning(message: string, title?: string, duration: number = 6000): void {
    this.show('warning', message, title, duration);
  }

  /**
   * Exibe toast informativo
   */
  info(message: string, title?: string, duration: number = 5000): void {
    this.show('info', message, title, duration);
  }

  /**
   * Remove um toast pelo ID
   */
  remove(id: number): void {
    const currentToasts = this.toastsSubject.value;
    this.toastsSubject.next(currentToasts.filter(t => t.id !== id));
  }

  /**
   * Remove todos os toasts
   */
  clear(): void {
    this.toastsSubject.next([]);
  }

  /**
   * Exibe um toast
   */
  private show(type: Toast['type'], message: string, title?: string, duration: number = 5000): void {
    const toast: Toast = {
      id: ++this.counter,
      type,
      title,
      message,
      duration
    };

    const currentToasts = this.toastsSubject.value;
    this.toastsSubject.next([...currentToasts, toast]);

    // Auto-remove após a duração
    if (duration > 0) {
      setTimeout(() => {
        this.remove(toast.id);
      }, duration);
    }
  }
}
