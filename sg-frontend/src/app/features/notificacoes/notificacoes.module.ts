import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { SharedModule } from '../../shared/shared.module';
import { GenericListComponent } from '../../shared/generic-list/generic-list.component';

const routes: Routes = [
  { path: '', component: GenericListComponent, data: { title: 'Notificações', api: '/api/notificacoes', icon: 'bi-bell', columns: ['nome', 'atividade', 'hora', 'statusNot'] } }
];

@NgModule({ declarations: [], imports: [CommonModule, RouterModule.forChild(routes), FormsModule, SharedModule] })
export class NotificacoesModule { }
