import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { SharedModule } from '../../shared/shared.module';
import { GenericListComponent } from '../../shared/generic-list/generic-list.component';
import { GenericFormComponent } from '../../shared/generic-form/generic-form.component';

const routes: Routes = [
  { path: '', component: GenericListComponent, data: { title: 'Secretários', api: '/api/secretarios', icon: 'bi-journal-text', columns: ['nome', 'email', 'telefone'] } },
  { path: 'novo', component: GenericFormComponent, data: { title: 'Secretário', api: '/api/secretarios', fields: [
    { name: 'nome', label: 'Nome', type: 'text', required: true },
    { name: 'email', label: 'Email', type: 'email', required: true },
    { name: 'documento', label: 'Documento', type: 'text' },
    { name: 'telefone', label: 'Telefone', type: 'text' }
  ]}},
  { path: 'editar/:id', component: GenericFormComponent, data: { title: 'Secretário', api: '/api/secretarios', isEdit: true, fields: [
    { name: 'nome', label: 'Nome', type: 'text', required: true },
    { name: 'email', label: 'Email', type: 'email', required: true },
    { name: 'documento', label: 'Documento', type: 'text' },
    { name: 'telefone', label: 'Telefone', type: 'text' }
  ]}}
];

@NgModule({ declarations: [], imports: [CommonModule, RouterModule.forChild(routes), FormsModule, SharedModule] })
export class SecretariosModule { }
