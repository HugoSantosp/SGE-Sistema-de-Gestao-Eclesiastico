import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { SharedModule } from '../../shared/shared.module';
import { GenericListComponent } from '../../shared/generic-list/generic-list.component';
import { GenericFormComponent } from '../../shared/generic-form/generic-form.component';

const routes: Routes = [
  {
    path: '', component: GenericListComponent,
    data: { title: 'Profissionais', api: '/api/profissionais', icon: 'bi-briefcase', columns: ['nome', 'especialidade', 'telefone'] }
  },
  {
    path: 'novo', component: GenericFormComponent,
    data: { title: 'Profissional', api: '/api/profissionais', fields: [
      { name: 'nome', label: 'Nome', type: 'text', required: true },
      { name: 'especialidade', label: 'Especialidade', type: 'text' },
      { name: 'telefone', label: 'Telefone', type: 'text' },
      { name: 'email', label: 'Email', type: 'email' },
      { name: 'foto', label: 'URL da Foto', type: 'text' },
      { name: 'descricao', label: 'Descrição', type: 'textarea' }
    ]}
  },
  {
    path: 'editar/:id', component: GenericFormComponent,
    data: { title: 'Profissional', api: '/api/profissionais', isEdit: true, fields: [
      { name: 'nome', label: 'Nome', type: 'text', required: true },
      { name: 'especialidade', label: 'Especialidade', type: 'text' },
      { name: 'telefone', label: 'Telefone', type: 'text' },
      { name: 'email', label: 'Email', type: 'email' },
      { name: 'foto', label: 'URL da Foto', type: 'text' },
      { name: 'descricao', label: 'Descrição', type: 'textarea' }
    ]}
  }
];

@NgModule({
  declarations: [],
  imports: [CommonModule, RouterModule.forChild(routes), FormsModule, SharedModule]
})
export class ProfissionaisModule { }
