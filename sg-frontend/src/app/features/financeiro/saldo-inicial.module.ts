import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { SharedModule } from '../../shared/shared.module';
import { GenericListComponent } from '../../shared/generic-list/generic-list.component';
import { GenericFormComponent } from '../../shared/generic-form/generic-form.component';

const formFields = [
  { name: 'valor', label: 'Valor', type: 'number', required: true, min: 0 },
  { name: 'dataReferencia', label: 'Data de referência', type: 'date', required: true },
  { name: 'observacao', label: 'Observação', type: 'textarea', maxLength: 255 }
];

const routes: Routes = [
  {
    path: '',
    component: GenericListComponent,
    data: {
      title: 'Saldo Inicial',
      api: '/api/financeiro/saldo-inicial',
      icon: 'bi-wallet2',
      columns: ['valor', 'dataReferencia', 'observacao', 'dataCad'],
      columnLabels: {
        valor: 'Valor',
        dataReferencia: 'Data de Referência',
        observacao: 'Observação',
        dataCad: 'Registrado em'
      },
      currencyColumns: ['valor'],
      // Histórico é imutável: cada registro é um marco do saldo, sem edição nem exclusão
      canEdit: false,
      canDelete: false
    }
  },
  {
    path: 'novo',
    component: GenericFormComponent,
    data: { title: 'Saldo Inicial', api: '/api/financeiro/saldo-inicial', fields: formFields }
  }
];

@NgModule({
  declarations: [],
  imports: [CommonModule, RouterModule.forChild(routes), FormsModule, SharedModule]
})
export class SaldoInicialModule { }
