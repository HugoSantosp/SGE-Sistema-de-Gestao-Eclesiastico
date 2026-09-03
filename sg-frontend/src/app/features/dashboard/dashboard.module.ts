import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { DashboardRoutingModule } from './dashboard-routing.module';
import { DashboardPresidenteComponent } from './presidente/dashboard-presidente.component';
import { DashboardPastorComponent } from './pastor/dashboard-pastor.component';
import { DashboardTesoureiroComponent } from './tesoureiro/dashboard-tesoureiro.component';
import { DashboardSecretarioComponent } from './secretario/dashboard-secretario.component';

@NgModule({
  declarations: [
    DashboardPresidenteComponent,
    DashboardPastorComponent,
    DashboardTesoureiroComponent,
    DashboardSecretarioComponent
  ],
  imports: [CommonModule, RouterModule, DashboardRoutingModule]
})
export class DashboardModule { }
