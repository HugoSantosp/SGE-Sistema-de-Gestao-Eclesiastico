import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { SharedModule } from '../../shared/shared.module';
import { GenericListComponent } from '../../shared/generic-list/generic-list.component';
import { GenericFormComponent } from '../../shared/generic-form/generic-form.component';

const routes: Routes = [
  { path: '', component: GenericListComponent, data: { title: 'Fornecedores', api: '/api/fornecedores', icon: 'bi-truck', columns: ['nome', 'telefone', 'produto'] } },
  { path: 'novo', component: GenericFormComponent, data: { title: 'Fornecedor', api: '/api/fornecedores', fields: [
    { name: 'nome', label: 'Nome', type: 'text', required: true },
    { name: 'telefone', label: 'Telefone', type: 'text' },
    { name: 'email', label: 'Email', type: 'email' },
    { name: 'produto', label: 'Produto', type: 'text' },
    { name: 'endereco', label: 'Endereço', type: 'text' }
  ]}},
  { path: 'editar/:id', component: GenericFormComponent, data: { title: 'Fornecedor', api: '/api/fornecedores', isEdit: true, fields: [
    { name: 'nome', label: 'Nome', type: 'text', required: true },
    { name: 'telefone', label: 'Telefone', type: 'text' },
    { name: 'email', label: 'Email', type: 'email' },
    { name: 'produto', label: 'Produto', type: 'text' },
    { name: 'endereco', label: 'Endereço', type: 'text' }
  ]}}
];

@NgModule({ declarations: [], imports: [CommonModule, RouterModule.forChild(routes), FormsModule, SharedModule] })
export class FornecedoresModule { }
