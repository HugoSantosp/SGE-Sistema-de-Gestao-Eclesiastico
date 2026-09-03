import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { MmAuthService } from '../../core/auth/mm-auth.service';

@Component({
  selector: 'app-trocar-senha',
  templateUrl: './trocar-senha.component.html',
  styleUrls: ['./trocar-senha.component.scss']
})
export class TrocarSenhaComponent {
  senhaAtual = '';
  novaSenha = '';
  confirmacao = '';
  loading = false;
  error = '';
  sucesso = false;

  constructor(
    private authService: MmAuthService,
    private router: Router
  ) {}

  trocar(): void {
    if (!this.senhaAtual || !this.novaSenha) {
      this.error = 'Preencha todos os campos';
      return;
    }
    if (this.novaSenha.length < 6) {
      this.error = 'A nova senha deve ter pelo menos 6 caracteres';
      return;
    }
    if (this.novaSenha !== this.confirmacao) {
      this.error = 'A confirmação não confere com a nova senha';
      return;
    }

    this.loading = true;
    this.error = '';

    this.authService.alterarSenha({ senhaAtual: this.senhaAtual, novaSenha: this.novaSenha }).subscribe({
      next: () => {
        this.authService.marcarSenhaAlterada();
        this.sucesso = true;
        setTimeout(() => this.router.navigate(['/ministerios']), 1200);
      },
      error: (err) => {
        this.error = err.status === 401
          ? 'Senha atual incorreta'
          : 'Erro ao alterar a senha. Tente novamente.';
        this.loading = false;
      }
    });
  }

  sair(): void {
    this.authService.logout();
  }
}
