import { Component, OnInit, OnDestroy } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { filter, Subscription } from 'rxjs';
import { AuthService } from './core/auth/auth.service';

/**
 * Detecta se está rodando dentro do Capacitor (app nativo) ou no navegador.
 */
function isNativeApp(): boolean {
  try {
    return !!(
      (window as any).Capacitor?.isNativePlatform?.() ||
      (window as any).cordova
    );
  } catch {
    return false;
  }
}

@Component({
  selector: 'app-root',
  template: `
    <!-- WEB: Layout com sidebar (atual) -->
    <ng-container *ngIf="!isMobile">
      <!-- Login ou landing page sem sidebar -->
      <ng-container *ngIf="!isLoggedIn || isPublicPage; else webAdminLayout">
        <router-outlet></router-outlet>
      </ng-container>

      <!-- Admin web com sidebar + header -->
      <ng-template #webAdminLayout>
        <div class="app-layout">
          <app-sidebar [collapsed]="sidebarCollapsed"></app-sidebar>
          <div class="main-content" [class.sidebar-open]="!sidebarCollapsed">
            <app-header (toggleSidebar)="toggleSidebar()"></app-header>
            <div class="content-area">
              <router-outlet></router-outlet>
            </div>
          </div>
        </div>
      </ng-template>
    </ng-container>

    <!-- MOBILE (Capacitor): Layout com tab bar, sem landing page -->
    <ng-container *ngIf="isMobile">
      <router-outlet></router-outlet>
    </ng-container>
  `
})
export class AppComponent implements OnInit, OnDestroy {
  isLoggedIn = false;
  isPublicPage = false;
  isMobile = false;
  sidebarCollapsed = false;

  private currentUrl = '/';
  private authSub?: Subscription;
  private routerSub?: Subscription;

  constructor(
    private router: Router,
    private authService: AuthService
  ) {
    this.routerSub = this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: any) => {
      this.currentUrl = event.urlAfterRedirects || event.url;
      this.updatePublicPage();
    });
  }

  ngOnInit(): void {
    this.isMobile = isNativeApp();
    // Escuta o estado de autenticação REAL (não calcula pela URL)
    this.authSub = this.authService.user$.subscribe(user => {
      this.isLoggedIn = user !== null;
    });
    this.updatePublicPage();
  }

  ngOnDestroy(): void {
    this.authSub?.unsubscribe();
    this.routerSub?.unsubscribe();
  }

  private updatePublicPage(): void {
    const url = this.currentUrl;
    // Remove fragment da URL para verificação
    const cleanUrl = url.split('#')[0].split('?')[0];
    // Sem páginas públicas neste app: só o login fica sem o layout de admin.
    // (As páginas públicas do site vivem em build separado, na raiz do domínio.)
    const publicPages = ['/login'];
    this.isPublicPage = publicPages.some(p => cleanUrl.startsWith(p));
  }

  toggleSidebar(): void {
    this.sidebarCollapsed = !this.sidebarCollapsed;
  }
}
