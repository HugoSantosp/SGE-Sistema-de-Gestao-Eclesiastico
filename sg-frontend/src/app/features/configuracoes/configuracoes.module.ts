import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { SharedModule } from '../../shared/shared.module';
import { GenericListComponent } from '../../shared/generic-list/generic-list.component';

const routes: Routes = [
  { path: '', component: GenericListComponent, data: { title: 'Configurações', api: '/api/config', icon: 'bi-gear', columns: ['nome', 'valor'] } }
];

@NgModule({ declarations: [], imports: [CommonModule, RouterModule.forChild(routes), FormsModule, SharedModule] })
export class ConfiguracoesModule { }
