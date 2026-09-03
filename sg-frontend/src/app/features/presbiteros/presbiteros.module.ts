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
    data: { title: 'Presbíteros', api: '/api/presbiteros', icon: 'bi-person-check', columns: ['nome', 'email', 'telefone'] }
  },
  {
    path: 'novo',
    component: GenericFormComponent,    data: { title: 'Presbítero', api: '/api/presbiteros', fields: [
      { name: 'nome', label: 'Nome', type: 'text', required: true },
      { name: 'email', label: 'Email', type: 'email', required: true },
      { name: 'documento', label: 'Documento', type: 'text' },
      { name: 'telefone', label: 'Telefone', type: 'text' },
      { name: 'endereco', label: 'Endereço', type: 'text' },
      { name: 'foto', label: 'Foto', type: 'image', uploadHint: 'Foto do presbítero' },
      { name: 'obs', label: 'Observações', type: 'textarea' }
    ]}
  },
  {
    path: 'editar/:id', component: GenericFormComponent,
    data: { title: 'Presbítero', api: '/api/presbiteros', isEdit: true, fields: [
      { name: 'nome', label: 'Nome', type: 'text', required: true },
      { name: 'email', label: 'Email', type: 'email', required: true },
      { name: 'documento', label: 'Documento', type: 'text' },
      { name: 'telefone', label: 'Telefone', type: 'text' },
      { name: 'endereco', label: 'Endereço', type: 'text' },
      { name: 'foto', label: 'Foto', type: 'image', uploadHint: 'Foto do presbítero' },
      { name: 'obs', label: 'Observações', type: 'textarea' }
    ]}
  }
];

@NgModule({
  declarations: [],
  imports: [CommonModule, RouterModule.forChild(routes), FormsModule, SharedModule]
})
export class PresbiterosModule { }
