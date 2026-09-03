import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { MmAuthService } from '../auth/mm-auth.service';

@Component({
  selector: 'app-mm-mobile-layout',
  template: `
    <ion-app>
      <!-- Header -->
      <ion-header class="ion-no-border">
        <ion-toolbar>
          <ion-title>
            <div class="mm-mobile-brand">
              <img src="assets/logo-icert.png" alt="ICERT" class="mm-mobile-logo" />
              <span class="mm-mobile-title">MeuMinistério</span>
            </div>
          </ion-title>
          <ion-buttons slot="end">
            <ion-button (click)="logout()" color="dark">
              <ion-icon slot="icon-only" name="log-out-outline"></ion-icon>
            </ion-button>
          </ion-buttons>
        </ion-toolbar>
      </ion-header>

      <!-- Page Content -->
      <ion-content class="ion-padding">
        <router-outlet></router-outlet>
      </ion-content>

      <!-- Tab Bar (substitui a bottom-nav do web) -->
      <ion-tabs>
        <ion-tab-bar slot="bottom">
          <ion-tab-button (click)="navigate('ministerios')">
            <ion-icon name="home-outline"></ion-icon>
            <ion-label>Ministérios</ion-label>
          </ion-tab-button>

          <ion-tab-button (click)="navigate('escalas')">
            <ion-icon name="calendar-outline"></ion-icon>
            <ion-label>Escalas</ion-label>
          </ion-tab-button>
        </ion-tab-bar>
      </ion-tabs>
    </ion-app>
  `,
  styles: [`
    :host {
      --mm-mobile-header-height: 56px;
      --mm-mobile-tab-height: 56px;
    }

    ion-content {
      --background: var(--bg-body, #f7f8fa);
      --padding-top: 0;
      --padding-bottom: 0;
    }

    ion-toolbar {
      --background: #ffffff;
      --border-color: var(--border-color, #e5e7eb);
      --color: var(--text-primary, #111827);
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
    }

    .mm-mobile-brand {
      display: flex;
      align-items: center;
      gap: 10px;
    }

    .mm-mobile-logo {
      width: 30px;
      height: 30px;
      object-fit: contain;
    }

    .mm-mobile-title {
      font-size: 0.95rem;
      font-weight: 700;
      color: var(--text-primary, #111827);
    }

    /* Tab bar */
    ion-tab-bar {
      --background: #ffffff;
      --border: 1px solid var(--border-color, #e5e7eb);
      height: var(--mm-mobile-tab-height);
      padding: 4px 0;
      box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
    }

    ion-tab-button {
      --color: #9ca3af;
      --color-selected: #f97316;
      max-width: 120px;
    }

    ion-tab-button ion-icon {
      font-size: 1.35rem;
    }

    ion-tab-button ion-label {
      font-size: 0.65rem;
      font-weight: 500;
    }
  `]
})
export class MmMobileLayoutComponent {

  constructor(
    private router: Router,
    private auth: MmAuthService
  ) {
    // Marca o corpo para o styles.mobile.scss esconder a bottom-nav do web
    document.body.classList.add('mm-mobile');
  }

  navigate(route: string): void {
    this.router.navigate([route]);
  }

  logout(): void {
    this.auth.logout();
  }
}
