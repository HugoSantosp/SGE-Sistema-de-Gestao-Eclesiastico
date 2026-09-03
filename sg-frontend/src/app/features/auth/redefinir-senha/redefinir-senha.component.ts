import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-redefinir-senha',
  templateUrl: './redefinir-senha.component.html',
  styleUrls: ['./redefinir-senha.component.scss']
})
export class RedefinirSenhaComponent implements OnInit {
  token = '';
  novaSenha = '';
  confirmarSenha = '';
  loading = false;
  success = false;
  error = '';
  showSenha = false;
  showConfirmar = false;
  tokenInvalido = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.token = this.route.snapshot.queryParams['token'] || '';
    if (!this.token) {
      this.tokenInvalido = true;
      this.error = 'Link inválido ou expirado. Solicite uma nova redefinição.';
    }
  }

  redefinir(): void {
    this.error = '';

    if (!this.novaSenha || !this.confirmarSenha) {
      this.error = 'Preencha todos os campos';
      return;
    }

    if (this.novaSenha.length < 3) {
      this.error = 'A senha deve ter no mínimo 3 caracteres';
      return;
    }

    if (this.novaSenha !== this.confirmarSenha) {
      this.error = 'As senhas não conferem';
      return;
    }

    this.loading = true;

    this.authService.redefinirSenha({ token: this.token, novaSenha: this.novaSenha }).subscribe({
      next: () => {
        this.success = true;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.error = 'Token inválido ou expirado. Solicite uma nova redefinição.';
      }
    });
  }

  irParaLogin(): void {
    this.router.navigate(['/login']);
  }
}
