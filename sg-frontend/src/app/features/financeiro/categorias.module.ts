import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { SharedModule } from '../../shared/shared.module';
import { GenericListComponent } from '../../shared/generic-list/generic-list.component';
import { GenericFormComponent } from '../../shared/generic-form/generic-form.component';

const tiposOptions = [
  { value: 'RECEITA', label: 'Receita' },
  { value: 'DESPESA', label: 'Despesa' }
];

const formFields = [
  { name: 'nome', label: 'Nome', type: 'text', required: true, maxLength: 100 },
  { name: 'tipo', label: 'Tipo', type: 'select', required: true, options: tiposOptions }
];

const routes: Routes = [
  {
    path: '',
    component: GenericListComponent,
    data: {
      title: 'Categorias Financeiras',
      api: '/api/financeiro/categorias',
      icon: 'bi-tags',
      columns: ['nome', 'tipo', 'ativo'],
      columnLabels: { nome: 'Nome', tipo: 'Tipo', ativo: 'Ativa' },
      valueLabels: { tipo: { RECEITA: 'Receita', DESPESA: 'Despesa' } },
      filters: [{ key: 'tipo', label: 'Tipo', type: 'select', options: tiposOptions }],
      extraActions: [
        {
          label: 'Desativar', icon: 'bi bi-toggle-on', class: '#22c55e',
          api: '/api/financeiro/categorias/{id}/desativar', method: 'patch',
          confirm: 'Desativar esta categoria? Ela deixa de aparecer em novos lançamentos.',
          visibleWhen: { field: 'ativo', equals: true }
        },
        {
          label: 'Ativar', icon: 'bi bi-toggle-off', class: '#6b7280',
          api: '/api/financeiro/categorias/{id}/ativar', method: 'patch',
          confirm: '',
          visibleWhen: { field: 'ativo', equals: false }
        }
      ]
    }
  },
  {
    path: 'novo',
    component: GenericFormComponent,
    data: { title: 'Categoria Financeira', api: '/api/financeiro/categorias', fields: formFields }
  },
  {
    path: 'editar/:id',
    component: GenericFormComponent,
    data: { title: 'Categoria Financeira', api: '/api/financeiro/categorias', isEdit: true, fields: formFields }
  }
];

@NgModule({
  declarations: [],
  imports: [CommonModule, RouterModule.forChild(routes), FormsModule, SharedModule]
})
export class CategoriasModule { }
