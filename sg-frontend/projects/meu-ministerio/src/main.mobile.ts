import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';
import { AppMobileModule } from './app/app-mobile.module';

/**
 * Entry point para o App Mobile (Capacitor) do MeuMinisterio.
 * Faz bootstrap do AppMobileModule que inclui Ionic + layout com tab bar.
 */
platformBrowserDynamic().bootstrapModule(AppMobileModule)
  .catch(err => console.error('Erro ao iniciar MeuMinisterio Mobile:', err));
