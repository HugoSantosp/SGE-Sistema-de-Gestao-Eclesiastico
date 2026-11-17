import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { SharedModule } from '../../shared/shared.module';
import { RelatoriosComponent } from './relatorios.component';

const routes: Routes = [
  { path: '', component: RelatoriosComponent }
];

@NgModule({
  declarations: [RelatoriosComponent],
  imports: [CommonModule, RouterModule.forChild(routes), SharedModule]
})
export class RelatoriosModule { }
