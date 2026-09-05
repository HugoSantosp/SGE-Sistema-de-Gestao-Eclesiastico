import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule } from '@angular/common/http';
import { RouterModule, Routes } from '@angular/router';

import { SiteAppComponent } from './site-app.component';

/**
 * App PÚBLICO do site ICERT (servido na raiz do domínio).
 *
 * Build separado do painel administrativo: aqui só entram as páginas
 * públicas (Início, Ministérios, Células e Mural). O login/área restrita
 * vivem no app administrativo em /SGE-Administracao.
 */
const routes: Routes = [
  {
    path: '',
    loadChildren: () => import('../features/publico/publico.module').then(m => m.PublicoModule)
  },
  { path: '**', redirectTo: '' }
];

@NgModule({
  declarations: [SiteAppComponent],
  imports: [
    BrowserModule,
    HttpClientModule,
    RouterModule.forRoot(routes)
  ],
  bootstrap: [SiteAppComponent]
})
export class SiteAppModule { }
