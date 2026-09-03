import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { Subscription, filter } from 'rxjs';

@Component({
  selector: 'app-public-navbar',
  template: `
    <nav class="navbar navbar-expand-lg fixed-top px-4 py-3" 
         style="background:#ffffff;border-bottom:1px solid #e2e8f0;box-shadow:0 1px 3px rgba(0,0,0,0.04);">
      <div class="container">
        <a class="navbar-brand d-flex align-items-center gap-2" routerLink="/">
          <img src="assets/logo-icert.png" alt="ICERT" height="45">
          <span class="fw-bold fs-5" style="color:#1a1a2e;">ICERT</span>
        </a>
        <div class="d-flex align-items-center gap-3">
          <a class="nav-link" routerLink="/" routerLinkActive="active-link" [routerLinkActiveOptions]="{exact: true}" 
             style="color:#4a5568;">Início</a>
          <a class="nav-link" href="javascript:void(0)" (click)="navigateToLanding('sobre')" 
             style="color:#4a5568;">Sobre</a>
          <a class="nav-link" href="javascript:void(0)" (click)="navigateToLanding('pastores')" 
             style="color:#4a5568;">Liderança</a>
          <a class="nav-link" routerLink="/ministerios" routerLinkActive="active-link" 
             style="color:#4a5568;">Ministérios</a>
          <a class="nav-link" routerLink="/celulas" routerLinkActive="active-link" 
             style="color:#4a5568;">Células</a>
          <a class="nav-link" routerLink="/mural" routerLinkActive="active-link" 
             style="color:#4a5568;">Mural</a>
          <a class="nav-link" href="javascript:void(0)" (click)="navigateToLanding('eventos')" 
             style="color:#4a5568;">Eventos</a>
          <a class="nav-link" href="javascript:void(0)" (click)="navigateToLanding('horarios')" 
             style="color:#4a5568;">Horários</a>
          <a class="nav-link" href="javascript:void(0)" (click)="navigateToLanding('contato')" 
             style="color:#4a5568;">Contato</a>
          <a routerLink="/login" class="btn btn-sm fw-semibold px-4" 
             style="background:#f97316;color:#fff;border-radius:10px;">
            <i class="bi bi-box-arrow-in-right me-1"></i> Entrar
          </a>
        </div>
      </div>
    </nav>
  `,
  styles: [`
    .active-link {
      color: #1a1a2e !important;
      font-weight: 600;
    }
  `]
})
export class PublicNavbarComponent implements OnInit, OnDestroy {
  private routerSub?: Subscription;
  private isLandingPage = false;

  constructor(private router: Router) {}

  ngOnInit(): void {
    // Verifica se está na landing page
    this.checkIfLandingPage();

    // Escuta mudanças de rota
    this.routerSub = this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe(() => {
      this.checkIfLandingPage();
      // Processa fragment na URL
      const url = this.router.url;
      const fragmentIndex = url.indexOf('#');
      if (fragmentIndex !== -1) {
        const fragment = url.substring(fragmentIndex + 1);
        setTimeout(() => this.scrollTo(fragment), 100);
      }
    });
  }

  ngOnDestroy(): void {
    this.routerSub?.unsubscribe();
  }

  private checkIfLandingPage(): void {
    this.isLandingPage = this.router.url === '/' || this.router.url.startsWith('/#');
  }

  navigateToLanding(section: string): void {
    if (this.isLandingPage) {
      // Já está na landing page, faz scroll direto
      this.scrollTo(section);
    } else {
      // Navega para a landing page com o fragment
      this.router.navigate(['/'], { fragment: section });
    }
  }

  scrollTo(section: string): void {
    const element = document.getElementById(section);
    if (element) {
      element.scrollIntoView({ behavior: 'smooth' });
    }
  }
}
