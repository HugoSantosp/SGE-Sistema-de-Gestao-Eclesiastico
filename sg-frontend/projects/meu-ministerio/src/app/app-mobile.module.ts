import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { FormsModule } from '@angular/forms';

// Ionic
import { IonicModule } from '@ionic/angular';

import { AppMobileRoutingModule } from './app-mobile-routing.module';
import { AppComponent } from './app.component';
import { MmMobileLayoutComponent } from './core/mobile/mm-mobile-layout.component';
import { MmTokenInterceptor } from './core/auth/mm-token.interceptor';
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
 * Módulo principal para o App Mobile (Capacitor) do MeuMinisterio.
 * Diferenças do AppModule web:
 * - Importa IonicModule.forRoot()
 * - Usa AppMobileRoutingModule (mesmas rotas, mas com MmMobileLayoutComponent + tab bar)
 */
@NgModule({
  declarations: [
    AppComponent,
    MmMobileLayoutComponent,
    LoginComponent,
    TrocarSenhaComponent,
    MinisteriosComponent,
    MinisterioLiderComponent,
    EscalasComponent,
    EscalaDetalheComponent,
    EscalaGerenciarComponent,
    EscalaPublicaConfirmacaoComponent,
    EscalaPublicaResultadoComponent,
    EscalaPublicaMinistroComponent
  ],
  imports: [
    BrowserModule,
    AppMobileRoutingModule,
    HttpClientModule,
    FormsModule,
    IonicModule.forRoot({
      mode: 'md',
      backButtonText: '',
      animated: true
    })
  ],
  providers: [
    { provide: HTTP_INTERCEPTORS, useClass: MmTokenInterceptor, multi: true }
  ],
  bootstrap: [AppComponent]
})
export class AppMobileModule { }
