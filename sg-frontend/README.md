# SGE Frontend — siteICERT

Frontend do **SGE (Sistema de Gerenciamento Eclesiástico)** da Igreja ICERT — workspace Angular que reúne o painel administrativo, o módulo **Meu Ministério** (escalas de voluntariado no estilo Voluts) e as páginas públicas do site, além de versões mobile com Ionic/Capacitor.

> O backend (API Spring Boot) vive no repositório [SGE-Sistema-de-Gestao-Eclesiastico](https://github.com/HugoSantosp/SGE-Sistema-de-Gestao-Eclesiastico).

---

## Aplicações do workspace

| Aplicação | Pasta | Dev (porta) | Produção (baseHref) | Descrição |
|-----------|-------|-------------|---------------------|-----------|
| **SGE-Administração** (`sg-frontend`) | `src/` | `4200` | `/SGE-Administracao/` | Páginas públicas do site (Início, Ministérios, Células, Mural) + painel administrativo: dashboard, membros, bispos, presbíteros, ministérios, células, escalas de louvor, financeiro, usuários e mais |
| **Meu Ministério** (`meu-ministerio`) | `projects/meu-ministerio/` | `4300` | `/SGE-MeuMinisterio/` | Área do membro/voluntário: vínculos com ministérios, confirmação de disponibilidade e escalas. O líder do ministério gerencia membros e monta as escalas |
| **Mobile (Ionic/Capacitor)** | configurações `mobile` | — | — | Versão para Android/iOS (app admin e Meu Ministério) |

## Tecnologias

- **Angular 17** + TypeScript ~5.2 (workspace com múltiplos projetos)
- **Bootstrap 5** + Bootstrap Icons (identidade visual do admin/site)
- **Chart.js** (dashboards)
- **Ionic 7 + Capacitor 5** (apps mobile)
- **Cypress 13** (testes E2E) + **Karma/Jasmine** (testes unitários)

## Requisitos

- Node.js 18 ou 20
- Angular CLI 17 (`npm install -g @angular/cli@17`)
- Backend Spring Boot rodando em `http://localhost:8080` (o dev server faz proxy)

## Como rodar em desenvolvimento

```bash
npm install

# Painel administrativo + site público (http://localhost:4200)
npx ng serve sg-frontend

# Meu Ministério (http://localhost:4300)
npx ng serve meu-ministerio
```

As chamadas para `/auth`, `/api` e `/uploads` são redirecionadas pelo `proxy.conf.js` para o backend em `localhost:8080`.

## Build de produção

Os builds já usam os `baseHref` finais para servirem sob o mesmo domínio:

```bash
# Painel administrativo → dist/sg-frontend (baseHref /SGE-Administracao/)
npx ng build sg-frontend

# Meu Ministério → dist/meu-ministerio (baseHref /SGE-MeuMinisterio/)
npx ng build meu-ministerio

# Versões mobile (Ionic/Capacitor)
npx ng build sg-frontend --configuration mobile
npx ng build meu-ministerio --configuration mobile
```

Em produção, um nginx serve as duas aplicações em `/SGE-Administracao/` e `/SGE-MeuMinisterio/` (configuração de referência no repositório do backend, em `deploy/nginx.conf`).

## Estrutura resumida

```
src/app/                  → app sg-frontend (site + administração)
  core/                     → auth (JWT, guards, interceptors), componentes de layout
  features/
    publico/                → páginas públicas do site
    auth/                   → login, perfil, alterar senha
    dashboard/              → dashboards por papel
    <módulos admin>/        → membros, bispos, presbíteros, ministérios, células,
                              escalas, financeiro, usuários, etc.
  shared/                   → componentes genéricos (listas, formulários), pipes
projects/meu-ministerio/  → app Meu Ministério (membro/líder)
```

## Testes

```bash
# Unitários (Karma/Jasmine) com cobertura
npm run test:coverage

# E2E (Cypress) — abre o runner
npm run test:e2e

# E2E headless
npm run test:e2e:headless
```

Mais detalhes de E2E em [CYPRESS.md](./CYPRESS.md).

---

Feito para a Igreja ICERT ✝️
