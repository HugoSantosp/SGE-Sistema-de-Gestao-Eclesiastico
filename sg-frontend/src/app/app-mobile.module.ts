import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

// Ionic
import { IonicModule } from '@ionic/angular';

import { AppMobileRoutingModule } from './app-mobile-routing.module';
import { AppComponent } from './app.component';
import { MobileModule } from './core/mobile/mobile.module';
import { SidebarComponent } from './core/components/sidebar/sidebar.component';
import { HeaderComponent } from './core/components/header/header.component';
import { TokenInterceptor } from './core/auth/token.interceptor';

/**
 * Módulo principal para o App Mobile (Capacitor).
 * Diferenças do AppModule web:
 * - Importa IonicModule.forRoot()
 * - Usa AppMobileRoutingModule (sem landing page)
 * - Importa MobileModule (layout com tab bar)
 */
@NgModule({
  declarations: [
    AppComponent,
    SidebarComponent,
    HeaderComponent
  ],
  imports: [
    BrowserModule,
    AppMobileRoutingModule,
    HttpClientModule,
    FormsModule,
    ReactiveFormsModule,
    IonicModule.forRoot({
      mode: 'md',
      backButtonText: '',
      animated: true
    }),
    MobileModule
  ],
  providers: [
    { provide: HTTP_INTERCEPTORS, useClass: TokenInterceptor, multi: true }
  ],
  bootstrap: [AppComponent]
})
export class AppMobileModule { }
