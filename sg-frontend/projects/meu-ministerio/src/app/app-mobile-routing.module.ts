import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { MmAuthGuard } from './core/auth/mm-auth.guard';
import { MmMobileLayoutComponent } from './core/mobile/mm-mobile-layout.component';
import { LoginComponent } from './features/login/login.component';
import { TrocarSenhaComponent } from './features/trocar-senha/trocar-senha.component';
import { MinisteriosComponent } from './features/ministerios/ministerios.component';
import { MinisterioLiderComponent } from './features/ministerio-lider/ministerio-lider.component';
import { EscalasComponent } from './features/escalas/escalas.component';
import { EscalaDetalheComponent } from './features/escala-detalhe/escala-detalhe.component';
import { EscalaGerenciarComponent } from './features/escala-gerenciar/escala-gerenciar.component';
import { EscalaPublicaConfirmacaoComponent } from './features/escala-publico/confirmacao/confirmacao.component';
import { EscalaPublicaResultadoComponent } from './features/escala-publico/resultado/resultado.component';
import { EscalaPublicaMinistroComponent } from './features/escala-publico/ministro/ministro.component';

/**
 * Rotas para o App Mobile (Capacitor) do MeuMinisterio.
 * Mesmas rotas do web, mas com MmMobileLayoutComponent (ion-app + tab bar)
 * em vez do shell web com bottom-nav.
 */
const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'trocar-senha', component: TrocarSenhaComponent, canActivate: [MmAuthGuard] },
  // Rotas públicas da Escala de Louvor (acesso via token, sem login)
  // Ordem: mais específica primeiro (Angular prioriza, mas evita pegadinhas)
  { path: 'escala/resultado/:token/ministro', component: EscalaPublicaMinistroComponent },
  { path: 'escala/resultado/:token', component: EscalaPublicaResultadoComponent },
  { path: 'escala/:token', component: EscalaPublicaConfirmacaoComponent },
  {
    path: '',
    component: MmMobileLayoutComponent,
    canActivate: [MmAuthGuard],
    children: [
      { path: '', redirectTo: 'ministerios', pathMatch: 'full' },
      { path: 'ministerios', component: MinisteriosComponent },
      { path: 'ministerios/:id/gerenciar', component: MinisterioLiderComponent },
      { path: 'escalas', component: EscalasComponent },
      { path: 'escalas/:id', component: EscalaDetalheComponent },
      { path: 'escalas/:id/gerenciar', component: EscalaGerenciarComponent }
    ]
  },
  { path: '**', redirectTo: '' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppMobileRoutingModule { }
