import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { SharedModule } from '../../shared/shared.module';
import { GenericListComponent } from '../../shared/generic-list/generic-list.component';
import { GenericFormComponent } from '../../shared/generic-form/generic-form.component';

const routes: Routes = [
  { path: '', component: GenericListComponent, data: { title: 'Cargos Eclesiásticos', api: '/api/cargos', icon: 'bi-tags', columns: ['nome'] } },
  { path: 'novo', component: GenericFormComponent, data: { title: 'Cargo', api: '/api/cargos', fields: [
    { name: 'nome', label: 'Nome do Cargo', type: 'text', required: true }
  ]}},
  { path: 'editar/:id', component: GenericFormComponent, data: { title: 'Cargo', api: '/api/cargos', isEdit: true, fields: [
    { name: 'nome', label: 'Nome do Cargo', type: 'text', required: true }
  ]}}
];

@NgModule({ declarations: [], imports: [CommonModule, RouterModule.forChild(routes), FormsModule, SharedModule] })
export class CargosModule { }
