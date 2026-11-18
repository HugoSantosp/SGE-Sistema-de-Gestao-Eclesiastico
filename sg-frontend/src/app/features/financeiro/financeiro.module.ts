import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { SharedModule } from '../../shared/shared.module';
import { DashboardFinanceiroComponent } from './dashboard/dashboard-financeiro.component';
import { RelatoriosFinanceirosComponent } from './relatorios/relatorios-financeiros.component';

const routes: Routes = [
  {
    path: 'dashboard',
    component: DashboardFinanceiroComponent
  },
  {
    path: 'relatorios',
    component: RelatoriosFinanceirosComponent
  },
  {
    path: 'categorias',
    loadChildren: () => import('./categorias.module').then(m => m.CategoriasModule)
  },
  {
    path: 'saldo-inicial',
    loadChildren: () => import('./saldo-inicial.module').then(m => m.SaldoInicialModule)
  },
  {
    path: 'pagar',
    loadChildren: () => import('./contas-pagar.module').then(m => m.ContasPagarModule)
  },
  {
    path: 'receber',
    loadChildren: () => import('./contas-receber.module').then(m => m.ContasReceberModule)
  },
  {
    path: 'doacoes',
    loadChildren: () => import('./doacoes.module').then(m => m.DoacoesModule)
  },
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
];

@NgModule({
  declarations: [
    DashboardFinanceiroComponent,
    RelatoriosFinanceirosComponent
  ],
  imports: [CommonModule, RouterModule.forChild(routes), FormsModule, SharedModule]
})
export class FinanceiroModule { }
