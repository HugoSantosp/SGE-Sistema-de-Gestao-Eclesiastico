import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { SharedModule } from '../../shared/shared.module';

const routes: Routes = [
  {
    path: 'pagar',
    loadChildren: () => import('./contas-pagar.module').then(m => m.ContasPagarModule)
  },
  {
    path: 'receber',
    loadChildren: () => import('./contas-receber.module').then(m => m.ContasReceberModule)
  },
  { path: '', redirectTo: 'pagar', pathMatch: 'full' }
];

@NgModule({
  imports: [CommonModule, RouterModule.forChild(routes), FormsModule, SharedModule]
})
export class FinanceiroModule { }
