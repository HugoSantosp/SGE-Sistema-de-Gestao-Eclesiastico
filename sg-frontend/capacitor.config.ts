import { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.sge.icert',
  appName: 'SGE - ICERT',
  webDir: 'dist/sg-frontend',
  bundledWebRuntime: false,
  server: {
    androidScheme: 'https',
    cleartext: true // Permitir HTTP para API local em desenvolvimento
  },
  android: {
    allowMixedContent: true,
    backgroundColor: '#f5f7fa'
  }
};

export default config;
