import { CapacitorConfig } from '@capacitor/cli';

/**
 * Configuração do Capacitor para o app MeuMinisterio.
 *
 * Uso (a partir da pasta sg-frontend):
 *   npx cap add android --config capacitor.meu-ministerio.config.ts
 *   npx cap sync --config capacitor.meu-ministerio.config.ts
 *
 * O webDir aponta para o build mobile: ng build meu-ministerio --configuration mobile
 */
const config: CapacitorConfig = {
  appId: 'com.icert.meuministerio',
  appName: 'MeuMinistério - ICERT',
  webDir: 'dist/meu-ministerio-mobile',
  bundledWebRuntime: false,
  server: {
    androidScheme: 'https',
    cleartext: true // Permitir HTTP para API local em desenvolvimento
  },
  android: {
    allowMixedContent: true,
    backgroundColor: '#f7f8fa'
  }
};

export default config;
