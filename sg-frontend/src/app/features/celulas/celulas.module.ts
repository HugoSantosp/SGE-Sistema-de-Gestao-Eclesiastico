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
    data: { title: 'Células', api: '/api/celulas', icon: 'bi-people', columns: ['nome', 'lider', 'diaSemana', 'horario'] }
  },
  {
    path: 'novo', component: GenericFormComponent,
    data: { title: 'Célula', api: '/api/celulas', fields: [
      { name: 'nome', label: 'Nome', type: 'text', required: true },
      { name: 'lider', label: 'Líder', type: 'text' },
      { name: 'endereco', label: 'Endereço', type: 'text' },
      { name: 'diaSemana', label: 'Dia da Semana', type: 'text' },
      { name: 'horario', label: 'Horário', type: 'time' },
      { name: 'descricao', label: 'Descrição', type: 'textarea' },
      { name: 'foto', label: 'Foto', type: 'image', uploadHint: 'Foto da célula' }
    ]}
  },
  {
    path: 'editar/:id', component: GenericFormComponent,
    data: { title: 'Célula', api: '/api/celulas', isEdit: true, fields: [
      { name: 'nome', label: 'Nome', type: 'text', required: true },
      { name: 'lider', label: 'Líder', type: 'text' },
      { name: 'endereco', label: 'Endereço', type: 'text' },
      { name: 'diaSemana', label: 'Dia da Semana', type: 'text' },
      { name: 'horario', label: 'Horário', type: 'time' },
      { name: 'descricao', label: 'Descrição', type: 'textarea' },
      { name: 'foto', label: 'Foto', type: 'image', uploadHint: 'Foto da célula' }
    ]}
  }
];

@NgModule({
  declarations: [],
  imports: [CommonModule, RouterModule.forChild(routes), FormsModule, SharedModule]
})
export class CelulasModule { }
