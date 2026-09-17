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
          <!-- Mobile: fundo escurecido atrás da gaveta do menu (fecha ao toque) -->
          <div class="sidebar-backdrop" [class.show]="isSmallScreen && !sidebarCollapsed" (click)="toggleSidebar()"></div>
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
  isSmallScreen = false;
  sidebarCollapsed = false;

  private currentUrl = '/';
  private authSub?: Subscription;
  private routerSub?: Subscription;
  private smallScreenQuery?: MediaQueryList;

  constructor(
    private router: Router,
    private authService: AuthService
  ) {
    this.routerSub = this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: any) => {
      this.currentUrl = event.urlAfterRedirects || event.url;
      this.updatePublicPage();
      // No mobile o menu é uma gaveta: fecha ao navegar para o conteúdo aparecer
      if (this.isSmallScreen) {
        this.sidebarCollapsed = true;
      }
    });
  }

  ngOnInit(): void {
    this.isMobile = isNativeApp();
    this.setupSmallScreenWatcher();
    // Escuta o estado de autenticação REAL (não calcula pela URL)
    this.authSub = this.authService.user$.subscribe(user => {
      this.isLoggedIn = user !== null;
    });
    this.updatePublicPage();
  }

  ngOnDestroy(): void {
    this.authSub?.unsubscribe();
    this.routerSub?.unsubscribe();
    this.smallScreenQuery?.removeEventListener('change', this.onSmallScreenChange);
  }

  /**
   * Acompanha o breakpoint do layout (mesmo valor do CSS: 768px).
   * Em telas pequenas o menu começa fechado, pois vira uma gaveta sobre o conteúdo.
   */
  private setupSmallScreenWatcher(): void {
    this.smallScreenQuery = window.matchMedia('(max-width: 768px)');
    this.isSmallScreen = this.smallScreenQuery.matches;
    this.sidebarCollapsed = this.isSmallScreen;
    this.smallScreenQuery.addEventListener('change', this.onSmallScreenChange);
  }

  private onSmallScreenChange = (event: MediaQueryListEvent): void => {
    this.isSmallScreen = event.matches;
    // Ao entrar no mobile fecha a gaveta; ao voltar para o desktop reabre o menu
    this.sidebarCollapsed = event.matches;
  };

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
