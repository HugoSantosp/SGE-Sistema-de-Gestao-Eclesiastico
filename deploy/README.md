# SGE - Deploy (Fase 4)

Dois sistemas no mesmo domínio:

| URL | App | Build |
|---|---|---|
| `icertag.com.br/SGE-Administracao` | SGE admin (web) | `sg-frontend` |
| `icertag.com.br/SGE-MeuMinisterio` | App MeuMinisterio (estilo Voluts) | `meu-ministerio` |
| `icertag.com.br/api/*` e `/auth/*` | Spring Boot (backend) | `sge-backend` |
| `icertag.com.br/uploads/*` | Fotos enviadas | backend serve estático |

## 1. Build dos apps

```bash
./deploy/build.sh          # os dois apps
./deploy/build.sh admin    # só o admin
./deploy/build.sh ministerio # só o MeuMinisterio
```

Saídas:
- `sg-frontend/dist/sg-frontend/` — **base-href `/SGE-Administracao/`**
- `sg-frontend/dist/meu-ministerio/` — **base-href `/SGE-MeuMinisterio/`**

## 2. Backend (Spring Boot)

```bash
cd sg-backend
mvn clean package -DskipTests
java -jar target/sge-backend-1.0.0.jar
```

**Env vars obrigatórias em produção** (definidas em `application-prod.yml`):

| Variável | Exemplo |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/sge` |
| `DATABASE_USERNAME` | `sge_user` |
| `DATABASE_PASSWORD` | `***` |
| `JWT_SECRET` | string longa (>= 32 bytes) |
| `APP_BASE_URL` | `https://icertag.com.br/SGE-Administracao` |
| `ADMIN_INITIAL_PASSWORD` | senha inicial do admin (sai temporária) |
| `CORS_ALLOWED_ORIGINS` | `https://icertag.com.br` |

> ⚠️ **Sem Flyway** (desativado): o Hibernate cria/atualiza o schema (`ddl-auto: update`).
> Antes de todo deploy: `pg_dump` do banco.

## 3. Nginx

Copie `deploy/nginx.conf` para `/etc/nginx/conf.d/sge.conf` (ou `sites-available/sge`)
e ajuste o `root` (`/var/www/sge/dist`).

```bash
sudo cp deploy/nginx.conf /etc/nginx/conf.d/sge.conf
sudo nginx -t && sudo systemctl reload nginx
```

Estrutura de pastas esperada:

```
/var/www/sge/
├── dist/
│   ├── sg-frontend/       ← build do admin
│   └── meu-ministerio/    ← build do MeuMinisterio
└── uploads/               ← fotos (se servir direto pelo Nginx)
```

## 4. Verificação pós-deploy

| Teste | URL |
|---|---|
| Admin | `https://icertag.com.br/SGE-Administracao/login` |
| MeuMinisterio | `https://icertag.com.br/SGE-MeuMinisterio/login` |
| Login via API | `POST /auth/login` |
| Swagger | `https://icertag.com.br/swagger-ui.html` |

## 5. Fluxo de dados entre os apps (importante)

- **MeuMinisterio** usa o **mesmo backend e o mesmo JWT** do admin (login compartilhado).
- As **páginas públicas de escala** (confirmação via token e resultado) são hospedadas pelo
  **app ADMIN** em `/SGE-Administracao/escala/...` — o app MeuMinisterio gera os links
  apontando para lá (`MM_CONFIG.adminAppBase`).
- Usuário **MEMBRO** criado no admin → sincroniza automaticamente com a tabela `membros`.
- Líder adiciona membros ao ministério pelo próprio app MeuMinisterio.
