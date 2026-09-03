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
import { UploadUrlPipe } from './pipes/upload-url.pipe';

@NgModule({
  declarations: [
    GenericListComponent,
    GenericFormComponent,
    AlterarSenhaComponent,
    ToastComponent,
    ConfirmDialogComponent,
    LoadingSpinnerComponent,
    PaginationComponent,
    SearchFilterComponent,
    UploadUrlPipe
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule
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
    UploadUrlPipe,
    // Módulos
    CommonModule,
    FormsModule,
    ReactiveFormsModule
  ]
})
export class SharedModule { }
