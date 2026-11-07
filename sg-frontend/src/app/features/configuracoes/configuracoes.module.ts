import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { SharedModule } from '../../shared/shared.module';
import { GenericListComponent } from '../../shared/generic-list/generic-list.component';
import { GenericFormComponent } from '../../shared/generic-form/generic-form.component';

const fields = [
  { name: 'nome', label: 'Nome', type: 'text', required: true },
  { name: 'valor', label: 'Valor', type: 'text', required: true },
  { name: 'qtdTarefa', label: 'Qtd. Tarefas', type: 'number' }
];

const routes: Routes = [
  {
    path: '',
    component: GenericListComponent,
    data: { title: 'Configurações', api: '/api/config', icon: 'bi-gear', columns: ['nome', 'valor'] }
  },
  {
    path: 'novo',
    component: GenericFormComponent,
    data: { title: 'Configuração', api: '/api/config', fields }
  },
  {
    path: 'editar/:id',
    component: GenericFormComponent,
    data: { title: 'Configuração', api: '/api/config', isEdit: true, fields }
  }
];

@NgModule({ declarations: [], imports: [CommonModule, RouterModule.forChild(routes), FormsModule, SharedModule] })
export class ConfiguracoesModule { }
