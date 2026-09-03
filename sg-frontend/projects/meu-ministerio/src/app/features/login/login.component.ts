import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { MmAuthService } from '../../core/auth/mm-auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  user = '';
  senha = '';
  loading = false;
  error = '';

  constructor(
    private authService: MmAuthService,
    private router: Router
  ) {}

  login(): void {
    if (!this.user.trim() || !this.senha.trim()) {
      this.error = 'Preencha todos os campos';
      return;
    }

    this.loading = true;
    this.error = '';

    this.authService.login({ user: this.user, senha: this.senha }).subscribe({
      next: () => {
        // Senha temporária → troca obrigatória; senão → ministérios
        this.router.navigate(['/ministerios']);
      },
      error: (err) => {
        this.error = err.status === 401
          ? 'Email/CPF ou senha inválidos'
          : 'Erro ao conectar ao servidor';
        this.loading = false;
      }
    });
  }
}
