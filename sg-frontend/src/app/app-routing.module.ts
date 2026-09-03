import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from './core/auth/auth.guard';
import { RoleGuard } from './core/auth/role.guard';

const routes: Routes = [
  // Public routes (no auth) — must be first to match root paths
  {
    path: '',
    loadChildren: () => import('./features/publico/publico.module').then(m => m.PublicoModule)
  },
  {
    path: 'login',
    loadChildren: () => import('./features/auth/auth.module').then(m => m.AuthModule)
  },
  {
    path: 'redefinir-senha',
    redirectTo: '/login/redefinir-senha',
    pathMatch: 'full'
  },
  // NOTA: as páginas públicas da Escala de Louvor (confirmação, resultado e
  // repertório do ministro) agora são hospedadas pelo app MeuMinistério
  // (icertag.com.br/SGE-MeuMinisterio). O admin gera links para lá via
  // core/config/app-config.ts (ministerioUrl).
  // Protected routes (require auth)
  {
    path: '',
    canActivate: [AuthGuard],
    children: [
      {
        path: 'perfil',
        loadChildren: () => import('./features/auth/perfil/perfil.module').then(m => m.PerfilModule)
      },
      {
        path: 'dashboard',
        loadChildren: () => import('./features/dashboard/dashboard.module').then(m => m.DashboardModule)
      },
      {
        path: 'membros',
        loadChildren: () => import('./features/membros/membros.module').then(m => m.MembrosModule)
      },
      {
        path: 'bispos',
        loadChildren: () => import('./features/bispos/bispos.module').then(m => m.BisposModule)
      },
      {
        path: 'presbiteros',
        loadChildren: () => import('./features/presbiteros/presbiteros.module').then(m => m.PresbiterosModule)
      },
      {
        path: 'tesoureiros',
        loadChildren: () => import('./features/tesoureiros/tesoureiros.module').then(m => m.TesoureirosModule)
      },
      {
        path: 'secretarios',
        loadChildren: () => import('./features/secretarios/secretarios.module').then(m => m.SecretariosModule)
      },
      {
        path: 'cargos',
        loadChildren: () => import('./features/cargos/cargos.module').then(m => m.CargosModule)
      },
      {
        path: 'fornecedores',
        loadChildren: () => import('./features/fornecedores/fornecedores.module').then(m => m.FornecedoresModule)
      },
      {
        path: 'financeiro',
        loadChildren: () => import('./features/financeiro/financeiro.module').then(m => m.FinanceiroModule)
      },
      {
        path: 'tarefas',
        loadChildren: () => import('./features/tarefas/tarefas.module').then(m => m.TarefasModule)
      },
      {
        path: 'notificacoes',
        loadChildren: () => import('./features/notificacoes/notificacoes.module').then(m => m.NotificacoesModule)
      },
      {
        path: 'configuracoes',
        loadChildren: () => import('./features/configuracoes/configuracoes.module').then(m => m.ConfiguracoesModule)
      },
      {
        path: 'usuarios',
        loadChildren: () => import('./features/usuarios/usuarios.module').then(m => m.UsuariosModule),
        canActivate: [RoleGuard],
        data: { roles: ['PASTOR_PRESIDENTE'] }
      },
      // Admin CRUD routes (under /admin/ prefix to avoid conflict with public pages)
      {
        path: 'admin/ministerios',
        loadChildren: () => import('./features/ministerios/ministerios.module').then(m => m.MinisteriosModule)
      },
      {
        path: 'admin/celulas',
        loadChildren: () => import('./features/celulas/celulas.module').then(m => m.CelulasModule)
      },
      {
        path: 'admin/profissionais',
        loadChildren: () => import('./features/profissionais/profissionais.module').then(m => m.ProfissionaisModule)
      },
      {
        path: 'eventos',
        loadChildren: () => import('./features/eventos/eventos.module').then(m => m.EventosModule)
      },
      // NOTA: as Escalas de Louvor foram movidas para o app MeuMinistério
      // (icertag.com.br/SGE-MeuMinisterio) — o líder do ministério cria e
      // monta as escalas por lá. O admin não possui mais tela de escalas.
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  },
  { path: '**', redirectTo: '' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
