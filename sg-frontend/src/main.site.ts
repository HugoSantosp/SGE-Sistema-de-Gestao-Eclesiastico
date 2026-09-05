import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';
import { SiteAppModule } from './app/site/site-app.module';

platformBrowserDynamic().bootstrapModule(SiteAppModule)
  .catch(err => console.error(err));
