import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ministerioUrl } from '../../core/config/app-config';

/**
 * Tela exibida para perfis sem acesso ao SGE-Administração.
 * Hoje é usada pelo perfil MEMBRO, que opera apenas no app Meu Ministério.
 */
@Component({
  selector: 'app-sem-acesso',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="sem-acesso-wrapper">
      <div class="sem-acesso-card">
        <div class="sem-acesso-icon">
          <i class="bi bi-shield-lock"></i>
        </div>
        <h1>Sem acesso ao SGE-Administração</h1>
        <p>
          Seu perfil (<strong>{{ nivel }}</strong>) não possui permissão para usar o
          sistema administrativo.
        </p>
        <p class="sem-acesso-hint">
          Seu acesso é pelo <strong>Meu Ministério</strong>, disponível apenas para quem
          está vinculado a um ministério. Se você ainda não está vinculado, procure a
          secretaria da igreja.
        </p>
        <a class="sem-acesso-btn" [href]="linkMeuMinisterio">
          <i class="bi bi-box-arrow-up-right"></i>
          Ir para o Meu Ministério
        </a>
      </div>
    </div>
  `,
  styles: [`
    .sem-acesso-wrapper {
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 24px;
      background: #f8fafc;
    }
    .sem-acesso-card {
      max-width: 520px;
      width: 100%;
      background: #fff;
      border: 1px solid #e2e8f0;
      border-radius: 18px;
      padding: 40px 32px;
      text-align: center;
      box-shadow: 0 18px 40px rgba(15, 23, 42, 0.08);
    }
    .sem-acesso-icon {
      width: 64px;
      height: 64px;
      margin: 0 auto 18px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 50%;
      background: rgba(249, 115, 22, 0.1);
      color: #f97316;
      font-size: 1.8rem;
    }
    h1 {
      font-size: 1.35rem;
      font-weight: 700;
      color: #1a1a2e;
      margin: 0 0 12px;
    }
    p {
      font-size: 0.95rem;
      color: #4a5568;
      line-height: 1.6;
      margin: 0 0 12px;
    }
    .sem-acesso-hint {
      font-size: 0.88rem;
      color: #718096;
      margin-bottom: 24px;
    }
    .sem-acesso-btn {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      padding: 12px 22px;
      border-radius: 10px;
      background: #f97316;
      color: #fff;
      font-weight: 600;
      text-decoration: none;
      transition: background 0.2s ease;
    }
    .sem-acesso-btn:hover { background: #ea6a0c; }
  `]
})
export class SemAcessoComponent {
  nivel = this.lerNivel();
  linkMeuMinisterio = ministerioUrl('/');

  private lerNivel(): string {
    try {
      const raw = localStorage.getItem('sge_user');
      return raw ? (JSON.parse(raw)?.nivel ?? 'MEMBRO') : 'MEMBRO';
    } catch {
      return 'MEMBRO';
    }
  }
}
