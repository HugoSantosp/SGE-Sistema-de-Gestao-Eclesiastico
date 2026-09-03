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
    data: { title: 'Contas a Pagar', api: '/api/financeiro/contas-pagar', icon: 'bi-arrow-down-circle', columns: ['descricao', 'valor', 'vencimento', 'status'] }
  },
  {
    path: 'novo',
    component: GenericFormComponent,
    data: { title: 'Conta a Pagar', api: '/api/financeiro/contas-pagar', fields: [
      { name: 'descricao', label: 'Descrição', type: 'text', required: true },
      { name: 'valor', label: 'Valor', type: 'number', required: true },
      { name: 'vencimento', label: 'Vencimento', type: 'date', required: true },
      { name: 'frequencia', label: 'Frequência', type: 'select', options: [
        {value:'UNICA',label:'Única'},{value:'MENSAL',label:'Mensal'},{value:'TRIMESTRAL',label:'Trimestral'},{value:'ANUAL',label:'Anual'}
      ]}
    ]}
  },
  {
    path: 'editar/:id',
    component: GenericFormComponent,
    data: { title: 'Conta a Pagar', api: '/api/financeiro/contas-pagar', isEdit: true, fields: [
      { name: 'descricao', label: 'Descrição', type: 'text', required: true },
      { name: 'valor', label: 'Valor', type: 'number', required: true },
      { name: 'vencimento', label: 'Vencimento', type: 'date', required: true },
      { name: 'frequencia', label: 'Frequência', type: 'select', options: [
        {value:'UNICA',label:'Única'},{value:'MENSAL',label:'Mensal'},{value:'TRIMESTRAL',label:'Trimestral'},{value:'ANUAL',label:'Anual'}
      ]}
    ]}
  }
];

@NgModule({
  declarations: [],
  imports: [CommonModule, RouterModule.forChild(routes), FormsModule, SharedModule]
})
export class ContasPagarModule { }
