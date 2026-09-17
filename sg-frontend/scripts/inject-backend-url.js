#!/usr/bin/env node
/**
 * Injects window.__BACKEND_URL__ into the built Angular index.html files.
 *
 * Uso:
 *   node scripts/inject-backend-url.js <backend-url>
 *
 * Exemplo (Render deploy):
 *   node scripts/inject-backend-url.js https://sge-sistema-de-gestao-eclesiastico.onrender.com
 *
 * Isso adiciona <script>window.__BACKEND_URL__='...';</script> antes de </head>
 * em todos os index.html gerados, permitindo que o frontend leia a URL do
 * backend em tempo de execução via getBackendUrl().
 *
 * Variável de ambiente opcional: BACKEND_URL (se não for passada via CLI).
 */

const fs = require('fs');
const path = require('path');

const BACKEND_URL = process.argv[2] || process.env.BACKEND_URL;

if (!BACKEND_URL) {
  console.error('Uso: node inject-backend-url.js <backend-url>');
  console.error('   ou: BACKEND_URL=<url> node inject-backend-url.js');
  process.exit(1);
}

const distDirs = [
  path.join(__dirname, '..', 'dist', 'sg-frontend'),
  path.join(__dirname, '..', 'dist', 'site'),
  path.join(__dirname, '..', 'dist', 'meu-ministerio'),
  path.join(__dirname, '..', 'dist', 'meu-ministerio-mobile'),
];

const injectScript = `<script>window.__BACKEND_URL__='${BACKEND_URL.replace(/'/g, "\\'")}';</script>`;

let updated = 0;

for (const dir of distDirs) {
  if (!fs.existsSync(dir)) continue;

  const indexPath = path.join(dir, 'index.html');
  if (!fs.existsSync(indexPath)) continue;

  let html = fs.readFileSync(indexPath, 'utf-8');

  if (html.includes('window.__BACKEND_URL__')) {
    // Já foi injetado, substitui
    html = html.replace(
      /<script>window\.__BACKEND_URL__='[^']*';<\/script>/,
      injectScript
    );
  } else {
    // Injeta antes de </head>
    html = html.replace('</head>', injectScript + '</head>');
  }

  fs.writeFileSync(indexPath, html, 'utf-8');
  console.log(`  ✓ ${indexPath}`);
  updated++;
}

if (updated === 0) {
  console.warn('Nenhum index.html encontrado para injeção.');
  console.warn('Diretórios verificados:');
  for (const d of distDirs) {
    console.warn(`  ${d} ${fs.existsSync(d) ? '(existe)' : '(não existe)'}`);
  }
} else {
  console.log(`\n${updated} arquivo(s) atualizado(s).`);
}
