import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { EscalaListComponent } from './escala-list/escala-list.component';
import { EscalaFormComponent } from './escala-form/escala-form.component';
import { EscalaMontarComponent } from './escala-montar/escala-montar.component';

const routes: Routes = [
  { path: '', component: EscalaListComponent },
  { path: 'novo', component: EscalaFormComponent },
  { path: ':id', component: EscalaFormComponent },
  { path: ':id/montar', component: EscalaMontarComponent },
  { path: ':id/montar/:dataId', component: EscalaMontarComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class EscalaRoutingModule { }
