import { Component, ElementRef, ViewChild, AfterViewInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements AfterViewInit {
  @ViewChild('emailInput') emailInput!: ElementRef<HTMLInputElement>;

  user = '';
  senha = '';
  loading = false;
  error = '';
  hasError = false;
  showEsqueciSenha = false;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngAfterViewInit(): void {
    setTimeout(() => this.emailInput?.nativeElement?.focus(), 100);
  }

  login(): void {
    if (!this.user.trim() || !this.senha.trim()) {
      this.error = 'Preencha todos os campos';
      this.hasError = true;
      setTimeout(() => this.hasError = false, 500);
      return;
    }

    this.loading = true;
    this.error = '';

    this.authService.login({ user: this.user, senha: this.senha }).subscribe({
      next: (response) => {
        // Se senha temporária, força ir para o perfil trocar a senha
        if (response.senhaTemporaria) {
          this.router.navigate(['/perfil']);
        } else {
          this.router.navigate([this.authService.getDashboardRoute()]);
        }
      },
      error: (err) => {
        this.error = err.status === 401
          ? 'Email/CPF ou senha inválidos'
          : 'Erro ao conectar ao servidor';
        this.loading = false;
        this.hasError = true;
        setTimeout(() => this.hasError = false, 600);
      }
    });
  }

  abrirEsqueciSenha(): void {
    this.showEsqueciSenha = true;
  }

  fecharEsqueciSenha(): void {
    this.showEsqueciSenha = false;
  }
}
