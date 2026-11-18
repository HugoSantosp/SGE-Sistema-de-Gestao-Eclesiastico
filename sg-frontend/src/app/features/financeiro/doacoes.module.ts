import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { SharedModule } from '../../shared/shared.module';
import { GenericListComponent } from '../../shared/generic-list/generic-list.component';
import { GenericFormComponent } from '../../shared/generic-form/generic-form.component';

const tiposOptions = [
  { value: 'DIZIMO', label: 'Dízimo' },
  { value: 'OFERTA', label: 'Oferta' },
  { value: 'PRIMICIA', label: 'Primícia' }
];

const formasOptions = [
  { value: 'DINHEIRO', label: 'Dinheiro' },
  { value: 'PIX', label: 'PIX' },
  { value: 'CARTAO_DEBITO', label: 'Cartão de Débito' },
  { value: 'CARTAO_CREDITO', label: 'Cartão de Crédito' },
  { value: 'TRANSFERENCIA', label: 'Transferência' },
  { value: 'CHEQUE', label: 'Cheque' }
];

const formFields = [
  { name: 'tipo', label: 'Tipo', type: 'select', required: true, options: tiposOptions },
  {
    name: 'membroId', label: 'Membro', type: 'select', required: true,
    optionsApi: '/api/membros?situacao=ATIVO', optionLabel: 'nome',
    showWhen: { field: 'tipo', equals: ['DIZIMO', 'PRIMICIA'] }
  },
  {
    name: 'nomeDoador', label: 'Nome do doador', type: 'text', required: true,
    placeholder: 'Quem ofertou (não precisa ser membro)',
    showWhen: { field: 'tipo', equals: 'OFERTA' }
  },
  { name: 'dataDoacao', label: 'Data da doação', type: 'date', required: true },
  { name: 'formaPagamento', label: 'Forma de pagamento', type: 'select', required: true, options: formasOptions },
  { name: 'valor', label: 'Valor', type: 'number', required: true, min: 0.01 },
  { name: 'descricao', label: 'Descrição', type: 'textarea' }
];

const routes: Routes = [
  {
    path: '',
    component: GenericListComponent,
    data: {
      title: 'Doações',
      api: '/api/financeiro/doacoes',
      icon: 'bi-gift',
      columns: ['tipo', 'nomeDoador', 'dataDoacao', 'formaPagamento', 'valor'],
      columnLabels: {
        tipo: 'Tipo',
        nomeDoador: 'Doador',
        dataDoacao: 'Data',
        formaPagamento: 'Forma de Pagamento',
        valor: 'Valor'
      },
      valueLabels: {
        tipo: { DIZIMO: 'Dízimo', OFERTA: 'Oferta', PRIMICIA: 'Primícia' },
        formaPagamento: {
          DINHEIRO: 'Dinheiro',
          PIX: 'PIX',
          CARTAO_DEBITO: 'Cartão de Débito',
          CARTAO_CREDITO: 'Cartão de Crédito',
          TRANSFERENCIA: 'Transferência',
          CHEQUE: 'Cheque'
        }
      },
      currencyColumns: ['valor'],
      filters: [
        { key: 'tipo', label: 'Tipo', type: 'select', options: tiposOptions }
      ]
    }
  },
  {
    path: 'novo',
    component: GenericFormComponent,
    data: { title: 'Doação', api: '/api/financeiro/doacoes', fields: formFields }
  },
  {
    path: 'editar/:id',
    component: GenericFormComponent,
    data: { title: 'Doação', api: '/api/financeiro/doacoes', isEdit: true, fields: formFields }
  }
];

@NgModule({
  declarations: [],
  imports: [CommonModule, RouterModule.forChild(routes), FormsModule, SharedModule]
})
export class DoacoesModule { }
