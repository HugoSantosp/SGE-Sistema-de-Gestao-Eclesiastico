#!/usr/bin/env bash
# ============================================================
# Build de produção dos dois apps do SGE
# Requer: Node + npm instalados, dependências já baixadas.
#
# Uso:
#   ./deploy/build.sh            # build dos dois apps
#   ./deploy/build.sh admin      # apenas o admin
#   ./deploy/build.sh ministerio # apenas o MeuMinisterio
# ============================================================
set -euo pipefail

cd "$(dirname "$0")/../sg-frontend"

echo "==> Instalando dependências (se necessário)..."
[ -d node_modules ] || npm install

TARGET="${1:-all}"

if [ "$TARGET" = "all" ] || [ "$TARGET" = "admin" ]; then
  echo "==> Build do app ADMIN (base-href /SGE-Administracao/)..."
  npx ng build sg-frontend --configuration production
fi

if [ "$TARGET" = "all" ] || [ "$TARGET" = "ministerio" ]; then
  echo "==> Build do app MEU MINISTÉRIO (base-href /SGE-MeuMinisterio/)..."
  npx ng build meu-ministerio --configuration production
fi

echo "==> OK! Saídas em:"
echo "    sg-frontend/dist/sg-frontend      (app admin)"
echo "    sg-frontend/dist/meu-ministerio   (app MeuMinisterio)"
echo ""
echo "Copie o conteúdo de dist/ para /var/www/sge/dist/ (ou o root configurado no nginx)."
