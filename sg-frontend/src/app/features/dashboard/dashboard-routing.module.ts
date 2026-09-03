import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { RoleGuard } from '../../core/auth/role.guard';
import { DashboardPresidenteComponent } from './presidente/dashboard-presidente.component';
import { DashboardPastorComponent } from './pastor/dashboard-pastor.component';
import { DashboardTesoureiroComponent } from './tesoureiro/dashboard-tesoureiro.component';
import { DashboardSecretarioComponent } from './secretario/dashboard-secretario.component';

const routes: Routes = [
  {
    path: '',
    children: [
      {
        path: '',
        component: DashboardPresidenteComponent,
        canActivate: [RoleGuard],
        data: { roles: ['PASTOR_PRESIDENTE'] }
      },
      {
        path: 'pastor',
        component: DashboardPastorComponent,
        canActivate: [RoleGuard],
        data: { roles: ['PASTOR_AUXILIAR'] }
      },
      {
        path: 'tesoureiro',
        component: DashboardTesoureiroComponent,
        canActivate: [RoleGuard],
        data: { roles: ['TESOUREIRO'] }
      },
      {
        path: 'secretario',
        component: DashboardSecretarioComponent,
        canActivate: [RoleGuard],
        data: { roles: ['SECRETARIO'] }
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class DashboardRoutingModule { }
