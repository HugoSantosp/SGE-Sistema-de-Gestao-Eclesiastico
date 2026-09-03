import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { SharedModule } from '../../shared/shared.module';
import { LandingPageComponent } from './landing-page/landing-page.component';
import { MinisteriosPageComponent } from './ministerios-page/ministerios-page.component';
import { CelulasPageComponent } from './celulas-page/celulas-page.component';
import { MuralPageComponent } from './mural-page/mural-page.component';
import { PublicNavbarComponent } from './shared/public-navbar.component';

const routes: Routes = [
  { path: '', component: LandingPageComponent },
  { path: 'ministerios', component: MinisteriosPageComponent },
  { path: 'celulas', component: CelulasPageComponent },
  { path: 'mural', component: MuralPageComponent }
];

@NgModule({
  declarations: [
    LandingPageComponent,
    MinisteriosPageComponent,
    CelulasPageComponent,
    MuralPageComponent,
    PublicNavbarComponent
  ],
  imports: [CommonModule, RouterModule.forChild(routes), SharedModule]
})
export class PublicoModule { }
