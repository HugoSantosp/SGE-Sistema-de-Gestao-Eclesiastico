import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { SharedModule } from '../../shared/shared.module';
import { BisposListComponent } from './bispos-list/bispos-list.component';
import { BisposFormComponent } from './bispos-form/bispos-form.component';

const routes: Routes = [
  { path: '', component: BisposListComponent },
  { path: 'novo', component: BisposFormComponent },
  { path: 'editar/:id', component: BisposFormComponent }
];

@NgModule({
  declarations: [BisposListComponent, BisposFormComponent],
  imports: [CommonModule, RouterModule.forChild(routes), FormsModule, SharedModule]
})
export class BisposModule { }
