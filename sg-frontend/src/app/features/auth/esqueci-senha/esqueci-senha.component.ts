import { Component, EventEmitter, Output } from '@angular/core';
import { AuthService } from '../../../core/auth/auth.service';
import { EsqueciSenhaRequest } from '../../../core/models/sge.models';

@Component({
  selector: 'app-esqueci-senha',
  templateUrl: './esqueci-senha.component.html',
  styleUrls: ['./esqueci-senha.component.scss']
})
export class EsqueciSenhaComponent {
  @Output() fechou = new EventEmitter<void>();
  @Output() voltarLogin = new EventEmitter<void>();

  email = '';
  loading = false;
  sent = false;
  error = '';

  constructor(private authService: AuthService) {}

  enviar(): void {
    this.error = '';
    if (!this.email || !this.email.includes('@')) {
      this.error = 'Informe um email válido';
      return;
    }

    this.loading = true;
    const request: EsqueciSenhaRequest = { email: this.email };

    this.authService.esqueciSenha(request).subscribe({
      next: () => {
        this.sent = true;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        // Mesmo se o email não existir, não revelamos por segurança
        this.sent = true;
      }
    });
  }

  fechar(): void {
    this.fechou.emit();
  }

  voltarAoLogin(): void {
    this.voltarLogin.emit();
  }
}
