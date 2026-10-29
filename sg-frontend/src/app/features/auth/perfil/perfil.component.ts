import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../../core/auth/auth.service';
import { PerfilResponse } from '../../../core/models/sge.models';
import { getBackendUrl } from '../../../core/config/backend-config';

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

        const backendUrl = getBackendUrl();
        const uploadUrl = backendUrl ? backendUrl + '/api/upload' : '/api/upload';

        const uploadResult = await fetch(uploadUrl, {
          method: 'POST',
          headers: { 'Authorization': `Bearer ${this.authService.getToken()}` },
          body: formData
        });

        if (uploadResult.ok) {
          const data = await uploadResult.json();
          this.fotoUrl = data.url;
        } else {
          let msg = 'Erro ao fazer upload da foto';
          if (uploadResult.status === 413) {
            msg = 'Arquivo muito grande. Tamanho máximo permitido: 15 MB';
          } else {
            try { msg = (await uploadResult.json())?.error || msg; } catch { /* mantém mensagem padrão */ }
          }
          this.error = msg;
          this.uploading = false;
          this.loading = false;
          return;
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
      
      // ✅ IMPORTANTE: Se a URL já contém token (Signed URL), preservar ela completa!
      if (this.fotoUrl.includes('?token=') && this.fotoUrl.includes('&expires=')) {
        // URL assinada do backend - apenas adicionar domínio se necessário
        let backendUrl = getBackendUrl();
        
        // FALLBACK: Se não tiver backendUrl configurado, usa localhost:8080 (desenvolvimento)
        if (!backendUrl || backendUrl.trim() === '') {
          backendUrl = 'http://localhost:8080';
        }
        
        const cleanBackendUrl = backendUrl.replace(/\/$/, '');
        
        // Se já começa com /api/, só adicionar domínio
        if (this.fotoUrl.startsWith('/api/')) {
          return cleanBackendUrl + this.fotoUrl;
        }
        
        // Caso contrário, normalizar o path antes de adicionar domínio
        let path = this.fotoUrl;
        if (path.startsWith('/uploads/')) {
          path = '/api' + path;
        } else if (path.startsWith('uploads/')) {
          path = '/api/' + path;
        }
        return cleanBackendUrl + path;
      }
      
      // URL sem assinatura (backwards compatibility)
      let path = this.fotoUrl;
      
      // Fix: /uploads//uploads/ → /uploads/
      path = path.replace(/\/uploads\/+uploads\//g, '/uploads/');
      
      // Fix: uploads//uploads/ → uploads/
      path = path.replace(/^uploads\/+uploads\//g, 'uploads/');
      
      // Converte /uploads/ para /api/uploads/
      if (path.startsWith('/uploads/')) {
        path = '/api' + path;  // /uploads/2026/ → /api/uploads/2026/
      } else if (path.startsWith('uploads/')) {
        path = '/api/' + path;  // uploads/2026/ → /api/uploads/2026/
      } else if (!path.startsWith('/api/')) {
        // Se não começa com /api/ ou /uploads/, adiciona /api/
        path = path.startsWith('/') ? '/api' + path : '/api/' + path;
      }
      
      // Usa a URL do backend via variável de ambiente
      let backendUrl = getBackendUrl();
      
      // FALLBACK: Se não tiver backendUrl configurado, usa localhost:8080 (desenvolvimento)
      if (!backendUrl || backendUrl.trim() === '') {
        backendUrl = 'http://localhost:8080';
      }
      
      // Remove trailing slash do backendUrl se existir
      const cleanBackendUrl = backendUrl.replace(/\/$/, '');
      return cleanBackendUrl + path;  // http://localhost:8080/api/uploads/2026/...
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
