import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { EscalaRoutingModule } from './escala-routing.module';
import { EscalaListComponent } from './escala-list/escala-list.component';
import { EscalaFormComponent } from './escala-form/escala-form.component';
import { EscalaMontarComponent } from './escala-montar/escala-montar.component';

@NgModule({
  declarations: [EscalaListComponent, EscalaFormComponent, EscalaMontarComponent],
  imports: [CommonModule, EscalaRoutingModule, FormsModule]
})
export class EscalaModule { }
