import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { FormsModule } from '@angular/forms';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
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
import { UploadUrlPipe } from './shared/pipes/upload-url.pipe';

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    TrocarSenhaComponent,
    MinisteriosComponent,
    MinisterioLiderComponent,
    EscalasComponent,
    EscalaDetalheComponent,
    EscalaGerenciarComponent,
    EscalaPublicaConfirmacaoComponent,
    EscalaPublicaResultadoComponent,
    EscalaPublicaMinistroComponent,
    UploadUrlPipe
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    FormsModule
  ],
  providers: [
    { provide: HTTP_INTERCEPTORS, useClass: MmTokenInterceptor, multi: true }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
