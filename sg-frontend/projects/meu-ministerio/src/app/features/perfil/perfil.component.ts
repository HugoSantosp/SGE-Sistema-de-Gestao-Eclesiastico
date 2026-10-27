import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { MmAuthService } from '../../core/auth/mm-auth.service';
import { PerfilResponse, AtualizarPerfilRequest } from '../../core/models/mm.models';

@Component({
  selector: 'app-perfil',
  templateUrl: './perfil.component.html',
  styleUrls: ['./perfil.component.scss']
})
export class PerfilComponent implements OnInit {
  perfil: PerfilResponse | null = null;
  editando = false;
  loading = false;
  salvando = false;
  error = '';
  sucesso = false;

  // Dados editáveis
  nome = '';
  email = '';
  foto: string | null = null;
  fotoFile: File | null = null;
  fotoPreview: string | null = null;

  // Trocar senha
  showAlterarSenha = false;
  senhaAtual = '';
  novaSenha = '';
  confirmacaoSenha = '';
  salvandoSenha = false;
  sucessoSenha = false;
  errorSenha = '';
  showSenhaAtual = false;
  showNovaSenha = false;
  showConfirmacao = false;

  constructor(
    private authService: MmAuthService,
    public router: Router
  ) {}

  ngOnInit(): void {
    this.carregarPerfil();
  }

  carregarPerfil(): void {
    this.loading = true;
    this.error = '';
    this.authService.getPerfil().subscribe({
      next: (data) => {
        this.perfil = data;
        this.nome = data.nome;
        this.email = data.email;
        this.foto = data.foto;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erro ao carregar perfil. Tente novamente.';
        this.loading = false;
      }
    });
  }

  iniciarEdicao(): void {
    this.editando = true;
    this.sucesso = false;
    this.error = '';
  }

  cancelarEdicao(): void {
    if (this.perfil) {
      this.nome = this.perfil.nome;
      this.email = this.perfil.email;
      this.foto = this.perfil.foto;
    }
    this.fotoFile = null;
    this.fotoPreview = null;
    this.editando = false;
    this.error = '';
  }

  onFotoSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      const file = input.files[0];
      // Validar tamanho (5MB)
      if (file.size > 5 * 1024 * 1024) {
        this.error = 'Imagem muito grande. Máximo: 5MB';
        return;
      }
      // Validar tipo
      if (!file.type.startsWith('image/')) {
        this.error = 'Arquivo inválido. Selecione uma imagem.';
        return;
      }
      this.fotoFile = file;
      // Preview
      const reader = new FileReader();
      reader.onload = (e: any) => {
        this.fotoPreview = e.target.result;
      };
      reader.readAsDataURL(file);
      this.error = '';
    }
  }

  removerFoto(): void {
    this.fotoFile = null;
    this.fotoPreview = null;
    this.foto = null;
  }

  salvar(): void {
    if (!this.nome.trim()) {
      this.error = 'Nome é obrigatório';
      return;
    }
    if (!this.email.trim()) {
      this.error = 'Email é obrigatório';
      return;
    }
    // Validação básica de email
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(this.email)) {
      this.error = 'Email inválido';
      return;
    }

    this.salvando = true;
    this.error = '';
    this.sucesso = false;

    // TODO: Se fotoFile existir, fazer upload primeiro
    // Por enquanto, vamos apenas atualizar os dados textuais

    const request: AtualizarPerfilRequest = {
      nome: this.nome.trim(),
      email: this.email.trim(),
      foto: this.foto
    };

    this.authService.atualizarPerfil(request).subscribe({
      next: (data) => {
        this.perfil = data;
        this.editando = false;
        this.salvando = false;
        this.sucesso = true;
        setTimeout(() => this.sucesso = false, 3000);
      },
      error: (err) => {
        this.error = err.error?.message || 'Erro ao salvar perfil. Tente novamente.';
        this.salvando = false;
      }
    });
  }

  abrirAlterarSenha(): void {
    this.showAlterarSenha = true;
    this.senhaAtual = '';
    this.novaSenha = '';
    this.confirmacaoSenha = '';
    this.errorSenha = '';
    this.sucessoSenha = false;
    this.showSenhaAtual = false;
    this.showNovaSenha = false;
    this.showConfirmacao = false;
  }

  fecharAlterarSenha(): void {
    this.showAlterarSenha = false;
    this.senhaAtual = '';
    this.novaSenha = '';
    this.confirmacaoSenha = '';
    this.errorSenha = '';
    this.sucessoSenha = false;
  }

  toggleSenhaAtual(): void {
    this.showSenhaAtual = !this.showSenhaAtual;
  }

  toggleNovaSenha(): void {
    this.showNovaSenha = !this.showNovaSenha;
  }

  toggleConfirmacao(): void {
    this.showConfirmacao = !this.showConfirmacao;
  }

  alterarSenha(): void {
    if (!this.senhaAtual || !this.novaSenha) {
      this.errorSenha = 'Preencha todos os campos';
      return;
    }
    if (this.novaSenha.length < 3) {
      this.errorSenha = 'A nova senha deve ter pelo menos 3 caracteres';
      return;
    }
    if (this.novaSenha !== this.confirmacaoSenha) {
      this.errorSenha = 'A confirmação não confere com a nova senha';
      return;
    }

    this.salvandoSenha = true;
    this.errorSenha = '';

    this.authService.alterarSenha({ 
      senhaAtual: this.senhaAtual, 
      novaSenha: this.novaSenha 
    }).subscribe({
      next: () => {
        this.authService.marcarSenhaAlterada();
        this.sucessoSenha = true;
        setTimeout(() => this.fecharAlterarSenha(), 1500);
      },
      error: (err) => {
        this.errorSenha = err.status === 401
          ? 'Senha atual incorreta'
          : 'Erro ao alterar a senha. Tente novamente.';
        this.salvandoSenha = false;
      }
    });
  }

  sair(): void {
    this.authService.logout();
  }
}
