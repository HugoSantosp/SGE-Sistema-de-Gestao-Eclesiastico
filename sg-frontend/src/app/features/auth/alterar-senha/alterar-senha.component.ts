import { Component, EventEmitter, Output } from '@angular/core';
import { AuthService } from '../../../core/auth/auth.service';
import { AlterarSenhaRequest } from '../../../core/models/sge.models';

@Component({
  selector: 'app-alterar-senha',
  templateUrl: './alterar-senha.component.html',
  styleUrls: ['./alterar-senha.component.scss']
})
export class AlterarSenhaComponent {
  @Output() fechou = new EventEmitter<void>();
  @Output() senhaAlterada = new EventEmitter<void>();

  senhaAtual = '';
  novaSenha = '';
  confirmarSenha = '';
  loading = false;
  success = false;
  error = '';
  showSenhaAtual = false;
  showNovaSenha = false;
  showConfirmar = false;

  constructor(private authService: AuthService) {}

  alterar(): void {
    this.error = '';
    this.success = false;

    if (!this.senhaAtual || !this.novaSenha || !this.confirmarSenha) {
      this.error = 'Preencha todos os campos';
      return;
    }

    if (this.novaSenha.length < 3) {
      this.error = 'A nova senha deve ter no mínimo 3 caracteres';
      return;
    }

    if (this.novaSenha !== this.confirmarSenha) {
      this.error = 'As senhas não conferem';
      return;
    }

    this.loading = true;
    const request: AlterarSenhaRequest = {
      senhaAtual: this.senhaAtual,
      novaSenha: this.novaSenha
    };

    this.authService.alterarSenha(request).subscribe({
      next: () => {
        this.success = true;
        this.loading = false;
        this.senhaAtual = '';
        this.novaSenha = '';
        this.confirmarSenha = '';
        setTimeout(() => this.senhaAlterada.emit(), 2000);
      },
      error: (err) => {
        this.loading = false;
        this.error = err.status === 401
          ? 'Senha atual inválida'
          : 'Erro ao alterar senha. Tente novamente.';
      }
    });
  }

  fechar(): void {
    this.fechou.emit();
  }
}
