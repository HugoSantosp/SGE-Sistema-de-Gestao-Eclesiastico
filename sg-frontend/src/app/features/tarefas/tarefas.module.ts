import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { SharedModule } from '../../shared/shared.module';
import { GenericListComponent } from '../../shared/generic-list/generic-list.component';
import { GenericFormComponent } from '../../shared/generic-form/generic-form.component';

const routes: Routes = [
  { path: '', component: GenericListComponent, data: { title: 'Tarefas', api: '/api/tarefas', icon: 'bi-check2-square', columns: ['titulo', 'dataTarefa', 'horaTarefa', 'statusTarefa'] } },
  { path: 'novo', component: GenericFormComponent, data: { title: 'Tarefa', api: '/api/tarefas', fields: [
    { name: 'titulo', label: 'Título', type: 'text', required: true },
    { name: 'descricao', label: 'Descrição', type: 'textarea' },
    { name: 'dataTarefa', label: 'Data', type: 'date', required: true },
    { name: 'horaTarefa', label: 'Hora', type: 'time', required: true }
  ]}}
];

@NgModule({ declarations: [], imports: [CommonModule, RouterModule.forChild(routes), FormsModule, SharedModule] })
export class TarefasModule { }
