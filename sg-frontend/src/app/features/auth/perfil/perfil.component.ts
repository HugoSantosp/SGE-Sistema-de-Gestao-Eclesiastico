import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../../core/auth/auth.service';
import { PerfilResponse } from '../../../core/models/sge.models';

@Component({
  selector: 'app-perfil',
  templateUrl: './perfil.component.html',
  styleUrls: ['./perfil.component.scss']
})
export class PerfilComponent implements OnInit {
  profile: PerfilResponse | null = null;
  editando = false;
  senhaTemporaria = false;
  showAlterarSenha = false;

  // Form fields
  nome = '';
  email = '';
  fotoUrl = '';
  fotoFile: File | null = null;
  fotoPreview: string | null = null;
  loading = false;
  uploading = false;
  success = false;
  error = '';

  nivelLabel: Record<string, string> = {
    PASTOR_PRESIDENTE: 'Pastor Presidente',
    PASTOR_AUXILIAR: 'Pastor Auxiliar',
    TESOUREIRO: 'Tesoureiro',
    SECRETARIO: 'Secretário'
  };

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    this.carregarPerfil();
    // Verifica se está com senha temporária para abrir o modal automaticamente
    this.senhaTemporaria = this.authService.precisaTrocarSenha();
    if (this.senhaTemporaria) {
      setTimeout(() => this.abrirAlterarSenha(), 500);
    }
  }

  carregarPerfil(): void {
    this.authService.getPerfil().subscribe({
      next: (data) => {
        this.profile = data;
        this.nome = data.nome;
        this.email = data.email;
        this.fotoUrl = data.foto || '';
      },
      error: () => {
        this.error = 'Erro ao carregar perfil';
      }
    });
  }

  iniciarEdicao(): void {
    this.editando = true;
    this.success = false;
    this.error = '';
    this.fotoPreview = null;
    this.fotoFile = null;
  }

  cancelarEdicao(): void {
    this.editando = false;
    this.error = '';
    this.fotoPreview = null;
    this.fotoFile = null;
    if (this.profile) {
      this.nome = this.profile.nome;
      this.email = this.profile.email;
      this.fotoUrl = this.profile.foto || '';
    }
  }

  onFotoSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      this.fotoFile = input.files[0];
      const reader = new FileReader();
      reader.onload = (e) => {
        this.fotoPreview = e.target?.result as string;
      };
      reader.readAsDataURL(this.fotoFile);
    }
  }

  removerFoto(): void {
    this.fotoFile = null;
    this.fotoPreview = null;
    this.fotoUrl = '';
  }

  async salvar(): Promise<void> {
    this.error = '';
    this.success = false;

    if (!this.nome.trim() || !this.email.trim()) {
      this.error = 'Nome e email são obrigatórios';
      return;
    }

    this.loading = true;

    try {
      // Upload da foto se houver
      if (this.fotoFile) {
        this.uploading = true;
        const formData = new FormData();
        formData.append('file', this.fotoFile);

        const uploadResult = await fetch('/api/upload', {
          method: 'POST',
          headers: { 'Authorization': `Bearer ${this.authService.getToken()}` },
          body: formData
        });

        if (uploadResult.ok) {
          const data = await uploadResult.json();
          this.fotoUrl = data.url;
        }
        this.uploading = false;
      }

      this.authService.atualizarPerfil({
        nome: this.nome.trim(),
        email: this.email.trim(),
        foto: this.fotoUrl
      }).subscribe({
        next: (data) => {
          this.profile = data;
          this.editando = false;
          this.loading = false;
          this.success = true;
          this.fotoPreview = null;
          this.fotoFile = null;
          setTimeout(() => this.success = false, 3000);
        },
        error: (err) => {
          this.loading = false;
          this.uploading = false;
          this.error = err.status === 422
            ? err.error?.error || 'Email já está em uso'
            : 'Erro ao salvar perfil. Tente novamente.';
        }
      });
    } catch {
      this.loading = false;
      this.uploading = false;
      this.error = 'Erro ao fazer upload da foto';
    }
  }

  getFotoSrc(): string {
    if (this.fotoPreview) return this.fotoPreview;
    if (this.fotoUrl) {
      // Se já começa com http, retorna direto
      if (this.fotoUrl.startsWith('http')) return this.fotoUrl;
      // Para fotos locais, usa a URL absoluta do backend (porta 8080)
      // O Angular proxy não redireciona /uploads/ corretamente,
      // então acessamos direto no backend
      const backendUrl = 'http://localhost:8080';
      // Converte /uploads/ para /api/uploads/ para compatibilidade
      let path = this.fotoUrl.startsWith('/') ? this.fotoUrl : '/' + this.fotoUrl;
      if (path.startsWith('/uploads/') && !path.startsWith('/api/uploads/')) {
        path = '/api' + path;
      }
      return backendUrl + path;
    }
    return '';
  }

  abrirAlterarSenha(): void {
    this.showAlterarSenha = true;
  }

  fecharAlterarSenha(alterada: boolean): void {
    this.showAlterarSenha = false;
    if (alterada) {
      // Senha foi efetivamente alterada no backend — atualiza o localStorage
      this.authService.marcarSenhaAlterada();
    }
    this.senhaTemporaria = this.authService.precisaTrocarSenha();
    if (!this.senhaTemporaria) {
      this.carregarPerfil();
    }
  }
}
