import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { SharedModule } from '../../shared/shared.module';
import { GenericListComponent } from '../../shared/generic-list/generic-list.component';
import { GenericFormComponent } from '../../shared/generic-form/generic-form.component';
import { RoleGuard } from '../../core/auth/role.guard';

const extraActions = [
  {
    label: 'Resetar Senha',
    icon: 'bi bi-arrow-repeat',
    class: '#f97316',
    api: '/api/usuarios/{id}/resetar-senha',
    method: 'post',
    confirm: 'Tem certeza que deseja resetar a senha para 12345678? O usuário precisará trocar no próximo acesso.'
  }
];

const routes: Routes = [
  {
    path: '',
    component: GenericListComponent,
    canActivate: [RoleGuard],
    data: {
      title: 'Usuários', api: '/api/usuarios', icon: 'bi-people-fill',
      columns: ['nome', 'email', 'nivel', 'senhaTemporaria'],
      extraActions,
      roles: ['PASTOR_PRESIDENTE']
    }
  },
  {
    path: 'novo',
    component: GenericFormComponent,
    canActivate: [RoleGuard],
    data: { title: 'Usuário', api: '/api/usuarios', roles: ['PASTOR_PRESIDENTE'], fields: [
      { name: 'nome', label: 'Nome', type: 'text', required: true },
      { name: 'email', label: 'Email', type: 'email', required: true },
      { name: 'documento', label: 'Documento (CPF/RG — vincula o login ao cadastro de membro)', type: 'text', required: true },
      { name: 'nivel', label: 'Nível de Acesso', type: 'select', required: true, options: [
        { value: 'PASTOR_PRESIDENTE', label: '👑 Pastor Presidente' },
        { value: 'PASTOR_AUXILIAR', label: '🙏 Pastor Auxiliar' },
        { value: 'TESOUREIRO', label: '💰 Tesoureiro' },
        { value: 'SECRETARIO', label: '📋 Secretário' },
        { value: 'MEMBRO', label: '🧑 Membro' }
      ]},
      { name: 'foto', label: 'Foto de Perfil', type: 'image', uploadHint: 'Pastores aparecem na landing page' }
    ]}
  },
  {
    path: 'editar/:id',
    component: GenericFormComponent,
    canActivate: [RoleGuard],
    data: { title: 'Usuário', api: '/api/usuarios', isEdit: true, roles: ['PASTOR_PRESIDENTE'], fields: [
      { name: 'nome', label: 'Nome', type: 'text', required: true },
      { name: 'email', label: 'Email', type: 'email', required: true },
      { name: 'senha', label: 'Senha (deixe vazio para manter)', type: 'password' },
      { name: 'documento', label: 'Documento (CPF/RG — vincula o login ao cadastro de membro)', type: 'text', required: true },
      { name: 'nivel', label: 'Nível de Acesso', type: 'select', required: true, options: [
        { value: 'PASTOR_PRESIDENTE', label: '👑 Pastor Presidente' },
        { value: 'PASTOR_AUXILIAR', label: '🙏 Pastor Auxiliar' },
        { value: 'TESOUREIRO', label: '💰 Tesoureiro' },
        { value: 'SECRETARIO', label: '📋 Secretário' },
        { value: 'MEMBRO', label: '🧑 Membro' }
      ]},
      { name: 'foto', label: 'Foto de Perfil', type: 'image', uploadHint: 'Pastores aparecem na landing page' }
    ]}
  }
];

@NgModule({
  declarations: [],
  imports: [CommonModule, RouterModule.forChild(routes), FormsModule, SharedModule]
})
export class UsuariosModule { }
