# ============================================
# Build Stage - compila os 3 apps Angular
# (painel admin, site publico, meu ministerio)
# ============================================
FROM node:20-alpine AS build
WORKDIR /app

COPY sg-frontend/package*.json ./
RUN npm ci

COPY sg-frontend/ ./
RUN npm run build:all

# ============================================
# Runtime Stage - nginx servindo os 3 builds
# ============================================
FROM nginx:alpine

RUN rm -rf /usr/share/nginx/html/*

# Site público na raiz "/"
COPY --from=build /app/dist/site /usr/share/nginx/html

# Painel administrativo em /SGE-Administracao
COPY --from=build /app/dist/sg-frontend /usr/share/nginx/html/SGE-Administracao

# App Meu Ministério em /SGE-MeuMinisterio
COPY --from=build /app/dist/meu-ministerio /usr/share/nginx/html/SGE-MeuMinisterio

COPY deploy/nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=5s --start-period=15s --retries=3 \
    CMD wget -q -O /dev/null http://localhost/ || exit 1
