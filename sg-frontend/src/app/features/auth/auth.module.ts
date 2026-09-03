import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { LoginComponent } from './login/login.component';
import { EsqueciSenhaComponent } from './esqueci-senha/esqueci-senha.component';
import { RedefinirSenhaComponent } from './redefinir-senha/redefinir-senha.component';

// NOTA: A página "Meu Perfil" foi movida para um módulo próprio
// (features/auth/perfil/perfil.module.ts) para evitar que a rota /perfil,
// ao reutilizar este módulo, casasse com { path: '', component: LoginComponent }.
const routes: Routes = [
  { path: '', component: LoginComponent },
  { path: 'redefinir-senha', component: RedefinirSenhaComponent }
];

@NgModule({
  declarations: [
    LoginComponent,
    EsqueciSenhaComponent,
    RedefinirSenhaComponent
  ],
  imports: [CommonModule, RouterModule.forChild(routes), FormsModule],
  exports: [
    EsqueciSenhaComponent,
    RedefinirSenhaComponent
  ]
})
export class AuthModule { }
