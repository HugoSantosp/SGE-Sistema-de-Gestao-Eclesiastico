import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

// Componentes existentes
import { GenericListComponent } from './generic-list/generic-list.component';
import { GenericFormComponent } from './generic-form/generic-form.component';
import { AlterarSenhaComponent } from '../features/auth/alterar-senha/alterar-senha.component';

// Novos componentes de UI
import { ToastComponent } from './components/toast/toast.component';
import { ConfirmDialogComponent } from './components/confirm-dialog/confirm-dialog.component';
import { LoadingSpinnerComponent } from './components/loading-spinner/loading-spinner.component';
import { PaginationComponent } from './components/pagination/pagination.component';
import { SearchFilterComponent } from './components/search-filter/search-filter.component';

// Pipes compartilhados (UploadUrlPipe)
import { PipesModule } from './pipes/pipes.module';

@NgModule({
  declarations: [
    GenericListComponent,
    GenericFormComponent,
    AlterarSenhaComponent,
    ToastComponent,
    ConfirmDialogComponent,
    LoadingSpinnerComponent,
    PaginationComponent,
    SearchFilterComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule,
    PipesModule
  ],
  exports: [
    // Componentes existentes
    GenericListComponent,
    GenericFormComponent,
    AlterarSenhaComponent,
    // Novos componentes UI
    ToastComponent,
    ConfirmDialogComponent,
    LoadingSpinnerComponent,
    PaginationComponent,
    SearchFilterComponent,
    // Módulos
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    PipesModule
  ]
})
export class SharedModule { }
