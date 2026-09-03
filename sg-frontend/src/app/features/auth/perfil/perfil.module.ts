import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';
import { PerfilComponent } from './perfil.component';
import { SharedModule } from '../../../shared/shared.module';

/**
 * Módulo dedicado da página "Meu Perfil".
 * Separado do AuthModule para evitar que a rota /perfil case com o
 * LoginComponent (AuthModule define { path: '', component: LoginComponent }
 * e era carregado por duas rotas diferentes: /login e /perfil).
 */
const routes: Routes = [
  { path: '', component: PerfilComponent }
];

@NgModule({
  declarations: [PerfilComponent],
  imports: [
    CommonModule,
    RouterModule.forChild(routes),
    FormsModule,
    SharedModule // fornece <app-alterar-senha> (modal de troca de senha)
  ]
})
export class PerfilModule { }
