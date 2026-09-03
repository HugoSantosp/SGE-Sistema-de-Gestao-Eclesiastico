import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { SharedModule } from '../../shared/shared.module';
import { GenericListComponent } from '../../shared/generic-list/generic-list.component';
import { GenericFormComponent } from '../../shared/generic-form/generic-form.component';

const routes: Routes = [
  {
    path: '',
    component: GenericListComponent,
    data: { title: 'Ministérios', api: '/api/ministerios', icon: 'bi-people', columns: ['nome', 'descricao'] }
  },
  {
    path: 'novo',
    component: GenericFormComponent,
    data: { title: 'Ministério', api: '/api/ministerios', fields: [
      { name: 'nome', label: 'Nome', type: 'text', required: true },
      { name: 'descricao', label: 'Descrição', type: 'textarea' },
      { name: 'foto', label: 'Foto', type: 'image', uploadHint: 'Foto do ministério' }
    ]}
  },
  {
    path: 'editar/:id',
    component: GenericFormComponent,
    data: { title: 'Ministério', api: '/api/ministerios', isEdit: true, fields: [
      { name: 'nome', label: 'Nome', type: 'text', required: true },
      { name: 'descricao', label: 'Descrição', type: 'textarea' },
      { name: 'foto', label: 'Foto', type: 'image', uploadHint: 'Foto do ministério' }
    ]}
  }
];

@NgModule({
  declarations: [],
  imports: [CommonModule, RouterModule.forChild(routes), FormsModule, SharedModule]
})
export class MinisteriosModule { }
