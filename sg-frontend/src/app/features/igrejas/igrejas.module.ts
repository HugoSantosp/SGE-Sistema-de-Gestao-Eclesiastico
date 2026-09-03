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
    data: { title: 'Igrejas', api: '/api/igrejas', icon: 'bi-building', columns: ['nome', 'telefone', 'matriz'] }
  },
  {
    path: 'novo',
    component: GenericFormComponent,
    data: { title: 'Igreja', api: '/api/igrejas', fields: [
      { name: 'nome', label: 'Nome', type: 'text', required: true },
      { name: 'telefone', label: 'Telefone', type: 'text', required: true },
      { name: 'endereco', label: 'Endereço', type: 'text' },
      { name: 'matriz', label: 'Matriz', type: 'select', options: [{value:'Sim',label:'Sim'},{value:'Nao',label:'Não'}] },
      { name: 'obs', label: 'Observações', type: 'textarea' }
    ]}
  },
  {
    path: 'editar/:id',
    component: GenericFormComponent,
    data: { title: 'Igreja', api: '/api/igrejas', isEdit: true, fields: [
      { name: 'nome', label: 'Nome', type: 'text', required: true },
      { name: 'telefone', label: 'Telefone', type: 'text', required: true },
      { name: 'endereco', label: 'Endereço', type: 'text' },
      { name: 'matriz', label: 'Matriz', type: 'select', options: [{value:'Sim',label:'Sim'},{value:'Nao',label:'Não'}] },
      { name: 'obs', label: 'Observações', type: 'textarea' }
    ]}
  }
];

@NgModule({
  declarations: [],
  imports: [CommonModule, RouterModule.forChild(routes), FormsModule, SharedModule]
})
export class IgrejasModule { }
