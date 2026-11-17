import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MembrosListComponent } from './membros-list/membros-list.component';
import { MembrosFormComponent } from './membros-form/membros-form.component';
import { SharedModule } from '../../shared/shared.module';

const routes: Routes = [
  { path: '', component: MembrosListComponent },
  { path: 'novo', component: MembrosFormComponent },
  { path: 'editar/:id', component: MembrosFormComponent }
];

@NgModule({
  declarations: [MembrosListComponent, MembrosFormComponent],
  imports: [CommonModule, RouterModule.forChild(routes), FormsModule, ReactiveFormsModule, SharedModule]
})
export class MembrosModule { }
