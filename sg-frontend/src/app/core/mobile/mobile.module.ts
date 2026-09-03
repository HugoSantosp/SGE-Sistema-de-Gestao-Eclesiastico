import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MobileLayoutComponent } from './mobile-layout.component';

/**
 * Módulo que agrupa os componentes de layout mobile (Ionic).
 * O IonicModule é importado globalmente no AppModule.
 */
@NgModule({
  declarations: [
    MobileLayoutComponent
  ],
  imports: [
    CommonModule,
    RouterModule
  ],
  exports: [
    MobileLayoutComponent
  ]
})
export class MobileModule { }
