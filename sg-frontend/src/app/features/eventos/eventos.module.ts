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
    data: { title: 'Eventos', api: '/api/eventos', icon: 'bi-calendar-event', columns: ['titulo', 'data', 'hora', 'local'] }
  },
  {
    path: 'novo',
    component: GenericFormComponent,
    data: { title: 'Evento', api: '/api/eventos', fields: [
      { name: 'titulo', label: 'Título', type: 'text', required: true },
      { name: 'descricao', label: 'Descrição', type: 'textarea' },
      { name: 'data', label: 'Data', type: 'date', required: true },
      { name: 'hora', label: 'Hora', type: 'time' },
      { name: 'local', label: 'Local', type: 'text' }
    ]}
  },
  {
    path: 'editar/:id',
    component: GenericFormComponent,
    data: { title: 'Evento', api: '/api/eventos', isEdit: true, fields: [
      { name: 'titulo', label: 'Título', type: 'text', required: true },
      { name: 'descricao', label: 'Descrição', type: 'textarea' },
      { name: 'data', label: 'Data', type: 'date', required: true },
      { name: 'hora', label: 'Hora', type: 'time' },
      { name: 'local', label: 'Local', type: 'text' }
    ]}
  }
];

@NgModule({
  declarations: [],
  imports: [CommonModule, RouterModule.forChild(routes), FormsModule, SharedModule]
})
export class EventosModule { }
