import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';
import { AppMobileModule } from './app/app-mobile.module';

/**
 * Entry point para o App Mobile (Capacitor).
 * Faz bootstrap do AppMobileModule que inclui Ionic + routing sem landing page.
 */
platformBrowserDynamic().bootstrapModule(AppMobileModule)
  .catch(err => console.error('Erro ao iniciar SGE Mobile:', err));
