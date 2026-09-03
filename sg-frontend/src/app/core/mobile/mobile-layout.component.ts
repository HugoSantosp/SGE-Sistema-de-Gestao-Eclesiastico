import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-mobile-layout',
  template: `
    <ion-app>
      <!-- Header -->
      <ion-header class="ion-no-border">
        <ion-toolbar color="light">
          <ion-title>
            <div class="mobile-header-title">
              <img src="assets/logo2.png" alt="SGE" class="mobile-logo" />
              <span class="mobile-title-text">SGE - ICERT</span>
            </div>
          </ion-title>
          <ion-buttons slot="end">
            <ion-button (click)="logout()" color="medium">
              <ion-icon slot="icon-only" name="log-out-outline"></ion-icon>
            </ion-button>
          </ion-buttons>
        </ion-toolbar>
      </ion-header>

      <!-- Page Content -->
      <ion-content class="ion-padding">
        <router-outlet></router-outlet>
      </ion-content>

      <!-- Tab Bar (substitui a sidebar do desktop) -->
      <ion-tabs>
        <ion-tab-bar slot="bottom" color="light">
          <ion-tab-button tab="dashboard" (click)="navigate('dashboard')">
            <ion-icon name="home-outline"></ion-icon>
            <ion-label>Início</ion-label>
          </ion-tab-button>

          <ion-tab-button tab="membros" (click)="navigate('membros')">
            <ion-icon name="people-outline"></ion-icon>
            <ion-label>Membros</ion-label>
          </ion-tab-button>

          <ion-tab-button tab="financeiro" (click)="navigate('financeiro')">
            <ion-icon name="wallet-outline"></ion-icon>
            <ion-label>Financeiro</ion-label>
          </ion-tab-button>

          <ion-tab-button tab="tarefas" (click)="navigate('tarefas')">
            <ion-icon name="checkmark-circle-outline"></ion-icon>
            <ion-label>Tarefas</ion-label>
          </ion-tab-button>

          <ion-tab-button tab="mais" (click)="showMore = !showMore">
            <ion-icon name="ellipsis-horizontal-outline"></ion-icon>
            <ion-label>Mais</ion-label>
          </ion-tab-button>
        </ion-tab-bar>
      </ion-tabs>
    </ion-app>

    <!-- More Menu Overlay -->
    <div class="more-menu-overlay" *ngIf="showMore" (click)="showMore = false">
      <div class="more-menu-card" (click)="$event.stopPropagation()">
        <div class="more-menu-header">
          <h6>Navegação</h6>
          <ion-button fill="clear" size="small" (click)="showMore = false">
            <ion-icon name="close-outline"></ion-icon>
          </ion-button>
        </div>
        <div class="more-menu-items">
          <button *ngFor="let item of extraMenuItems" class="more-menu-item" (click)="navigate(item.route); showMore = false">
            <ion-icon [name]="item.icon" color="medium"></ion-icon>
            <span>{{ item.label }}</span>
          </button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    :host {
      --mobile-header-height: 56px;
      --mobile-tab-height: 56px;
    }

    ion-content {
      --background: #f5f7fa;
      --padding-top: 0;
      --padding-bottom: 0;
    }

    .mobile-header-title {
      display: flex;
      align-items: center;
      gap: 10px;
    }

    .mobile-logo {
      width: 32px;
      height: 32px;
      object-fit: contain;
      border-radius: 8px;
    }

    .mobile-title-text {
      font-size: 1rem;
      font-weight: 600;
      color: var(--text-primary, #1a1a2e);
    }

    /* Tab bar styling */
    ion-tab-bar {
      --background: #ffffff;
      --border: 1px solid #e2e8f0;
      height: var(--mobile-tab-height);
      padding: 4px 0;
      box-shadow: 0 -2px 10px rgba(0,0,0,0.05);
    }

    ion-tab-button {
      --color: #a0aec0;
      --color-selected: #f97316;
      max-width: 80px;
    }

    ion-tab-button ion-icon {
      font-size: 1.35rem;
    }

    ion-tab-button ion-label {
      font-size: 0.65rem;
      font-weight: 500;
    }

    /* More menu overlay */
    .more-menu-overlay {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0,0,0,0.3);
      z-index: 10000;
      display: flex;
      align-items: flex-end;
      justify-content: center;
      padding-bottom: 70px;
    }

    .more-menu-card {
      background: #fff;
      border-radius: 16px;
      width: calc(100% - 32px);
      max-width: 360px;
      overflow: hidden;
      box-shadow: 0 10px 40px rgba(0,0,0,0.15);
    }

    .more-menu-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 16px 20px 12px;
      border-bottom: 1px solid #e2e8f0;
    }

    .more-menu-header h6 {
      margin: 0;
      font-weight: 600;
      font-size: 0.9rem;
      color: #1a1a2e;
    }

    .more-menu-items {
      padding: 8px;
      display: flex;
      flex-direction: column;
      gap: 2px;
      max-height: 50vh;
      overflow-y: auto;
    }

    .more-menu-item {
      display: flex;
      align-items: center;
      gap: 14px;
      padding: 14px 16px;
      border: none;
      background: transparent;
      border-radius: 10px;
      cursor: pointer;
      font-size: 0.875rem;
      color: #4a5568;
      transition: background 0.2s;
      width: 100%;
      text-align: left;
    }

    .more-menu-item:hover {
      background: #f7f8fa;
    }

    .more-menu-item ion-icon {
      font-size: 1.25rem;
      min-width: 24px;
    }
  `]
})
export class MobileLayoutComponent {
  showMore = false;

  extraMenuItems = [
    { route: 'bispos', label: 'Bispos', icon: 'people-outline' },
    { route: 'presbiteros', label: 'Presbíteros', icon: 'people-outline' },
    { route: 'tesoureiros', label: 'Tesoureiros', icon: 'wallet-outline' },
    { route: 'secretarios', label: 'Secretários', icon: 'people-outline' },
    { route: 'cargos', label: 'Cargos', icon: 'pricetags-outline' },
    { route: 'fornecedores', label: 'Fornecedores', icon: 'car-outline' },
    { route: 'notificacoes', label: 'Notificações', icon: 'notifications-outline' },
    { route: 'eventos', label: 'Eventos', icon: 'calendar-outline' },
    { route: 'admin/ministerios', label: 'Ministérios', icon: 'heart-outline' },
    { route: 'admin/celulas', label: 'Células', icon: 'home-outline' },
    { route: 'admin/profissionais', label: 'Mural', icon: 'briefcase-outline' },
    { route: 'configuracoes', label: 'Configurações', icon: 'settings-outline' },
  ];

  constructor(private router: Router) {}

  navigate(route: string): void {
    this.router.navigate([route]);
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    this.router.navigate(['/login']);
  }
}
