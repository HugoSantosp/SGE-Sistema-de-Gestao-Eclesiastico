import { Component, Input, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../auth/auth.service';

interface MenuItem {
  label: string;
  icon: string;
  route?: string;
  children?: MenuItem[];
  expanded?: boolean;
  roles?: string[];
}

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styles: [`
    :host { display: contents; }
    .nav-links li a.active-link { background: #1a1a2e !important; color: #fff !important; }
    .nav-links li a.active-link i { color: #f97316 !important; }
    .rotate { transform: rotate(180deg); }
  `]
})
export class SidebarComponent implements OnInit {
  @Input() collapsed = false;
  user: any;
  userNivel = '';
  menuItems: MenuItem[] = [];

  private allMenuItems: MenuItem[] = [
    { label: 'Dashboard', icon: 'bi-grid', route: '/dashboard' },
    { label: 'Meu Perfil', icon: 'bi-person-circle', route: '/perfil' },
    {
      label: 'Pessoas', icon: 'bi-people', expanded: false,
      roles: ['PASTOR_PRESIDENTE', 'PASTOR_AUXILIAR', 'SECRETARIO'],
      children: [
        { label: 'Bispos', icon: 'bi-person-badge', route: '/bispos', roles: ['PASTOR_PRESIDENTE'] },
        { label: 'Presbíteros', icon: 'bi-person-check', route: '/presbiteros', roles: ['PASTOR_PRESIDENTE', 'PASTOR_AUXILIAR'] },
        { label: 'Tesoureiros', icon: 'bi-wallet2', route: '/tesoureiros', roles: ['PASTOR_PRESIDENTE', 'PASTOR_AUXILIAR'] },
        { label: 'Secretários', icon: 'bi-journal-text', route: '/secretarios', roles: ['PASTOR_PRESIDENTE', 'PASTOR_AUXILIAR'] },
        { label: 'Membros', icon: 'bi-person-lines-fill', route: '/membros', roles: ['PASTOR_PRESIDENTE', 'PASTOR_AUXILIAR', 'SECRETARIO'] },
        { label: 'Usuários', icon: 'bi-people-fill', route: '/usuarios', roles: ['PASTOR_PRESIDENTE'] },
      ]
    },
    {
      label: 'Cadastros', icon: 'bi-book', expanded: false,
      roles: ['PASTOR_PRESIDENTE', 'PASTOR_AUXILIAR'],
      children: [
        { label: 'Cargos', icon: 'bi-tags', route: '/cargos', roles: ['PASTOR_PRESIDENTE'] },
        { label: 'Ministérios', icon: 'bi-layers', route: '/admin/ministerios', roles: ['PASTOR_PRESIDENTE', 'PASTOR_AUXILIAR'] },
        { label: 'Células', icon: 'bi-people', route: '/admin/celulas', roles: ['PASTOR_PRESIDENTE', 'PASTOR_AUXILIAR'] },
        { label: 'Mural', icon: 'bi-briefcase', route: '/admin/profissionais', roles: ['PASTOR_PRESIDENTE', 'PASTOR_AUXILIAR'] },
        { label: 'Fornecedores', icon: 'bi-truck', route: '/fornecedores', roles: ['PASTOR_PRESIDENTE', 'TESOUREIRO'] },
      ]
    },
    {
      label: 'Financeiro', icon: 'bi-cash-coin', expanded: false,
      roles: ['PASTOR_PRESIDENTE', 'PASTOR_AUXILIAR', 'TESOUREIRO'],
      children: [
        { label: 'Contas à Pagar', icon: 'bi-arrow-down-circle', route: '/financeiro/pagar', roles: ['PASTOR_PRESIDENTE', 'PASTOR_AUXILIAR', 'TESOUREIRO'] },
        { label: 'Contas à Receber', icon: 'bi-arrow-up-circle', route: '/financeiro/receber', roles: ['PASTOR_PRESIDENTE', 'PASTOR_AUXILIAR', 'TESOUREIRO'] },
      ]
    },
    { label: 'Eventos', icon: 'bi-calendar-event', route: '/eventos', roles: ['PASTOR_PRESIDENTE', 'PASTOR_AUXILIAR', 'SECRETARIO'] },
    { label: 'Tarefas', icon: 'bi-check2-square', route: '/tarefas', roles: ['PASTOR_PRESIDENTE', 'PASTOR_AUXILIAR', 'SECRETARIO'] },
    { label: 'Notificações', icon: 'bi-bell', route: '/notificacoes', roles: ['PASTOR_PRESIDENTE', 'PASTOR_AUXILIAR', 'SECRETARIO'] },
    { label: 'Configurações', icon: 'bi-gear', route: '/configuracoes', roles: ['PASTOR_PRESIDENTE'] },
  ];

  constructor(
    private authService: AuthService,
    private router: Router
  ) {
    this.authService.user$.subscribe(u => {
      this.user = u;
      this.userNivel = u?.nivel || '';
      this.filterMenu();
    });
  }

  ngOnInit(): void {
    this.userNivel = this.authService.getNivel() || '';
    this.filterMenu();
  }

  private filterMenu(): void {
    if (!this.userNivel) {
      this.menuItems = [];
      return;
    }
    this.menuItems = this.allMenuItems
      .filter(item => this.hasAccess(item))
      .map(item => {
        if (item.children) {
          return {
            ...item,
            children: item.children.filter(child => this.hasAccess(child))
          };
        }
        return item;
      });
  }

  private hasAccess(item: MenuItem): boolean {
    if (!item.roles || item.roles.length === 0) return true;
    return item.roles.includes(this.userNivel);
  }

  toggleSubMenu(item: MenuItem): void {
    if (this.collapsed) return;
    item.expanded = !item.expanded;
  }

  logout(): void {
    this.authService.logout();
  }
}
