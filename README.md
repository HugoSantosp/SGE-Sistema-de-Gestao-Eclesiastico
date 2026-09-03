# 🏛️ SGE - Sistema de Gerenciamento Eclesiástico

[![Build Status](https://img.shields.io/badge/build-pending-orange)]()
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.java.com)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Angular](https://img.shields.io/badge/Angular-17-red.svg)](https://angular.io)

Sistema completo para gerenciamento de igrejas e ministérios, com funcionalidades de administração, escalas de louvor, finanças e muito mais.

---

## 📋 Índice

- [Funcionalidades](#funcionalidades)
- [Arquitetura](#arquitetura)
- [Pré-requisitos](#pré-requisitos)
- [Instalação](#instalação)
- [Configuração](#configuração)
- [Uso](#uso)
- [API Documentation](#api-documentation)
- [Deploy](#deploy)
- [Contribuição](#contribuição)
- [Licença](#licença)

---

## ✨ Funcionalidades

### Backend (Spring Boot)
- 🔐 **Autenticação JWT** com senhas temporárias
- 👥 **Gestão de Usuários** com sincronização de cargos
- ⛪ **Módulos**: Membros, Igrejas, Ministérios, Células, Eventos
- 💰 **Financeiro**: Contas a pagar/receber
- 📅 **Escalas de Louvor** com confirmação de disponibilidade
- 📊 **Relatórios** em PDF e Excel
- 📧 **Notificações por Email**
- 🛡️ **Segurança**: Rate limiting, headers, CORS
- 📈 **Monitoramento**: Health checks, métricas Prometheus

### Frontend (Angular)
- 🎨 **Interface Moderna** com identidade visual ICERT
- 📱 **Design Responsivo** para desktop e mobile
- 🔍 **Busca e Filtros** em todas as listagens
- ✅ **Validações** com mensagens em português
- 📊 **Dashboards** interativos
- 🎭 **Dois Apps**: Admin + MeuMinisterio (estilo Voluts)

---

## 🏗️ Arquitetura

```
┌─────────────────────────────────────────────────────────────┐
│                      SGE - Visão Geral                       │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐    │
│  │   Frontend  │    │   Backend   │    │   Database  │    │
│  │  (Angular)  │◄──►│(Spring Boot)│◄──►│ (PostgreSQL)│    │
│  │  Porta 4200 │    │  Porta 8080 │    │  Porta 5432 │    │
│  └─────────────┘    └─────────────┘    └─────────────┘    │
│         │                  │                  │            │
│         │                  │                  │            │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐    │
│  │ MeuMinisterio│    │   Redis     │    │   Sentry    │    │
│  │  Porta 4300 │    │   (Cache)   │    │  (Erros)    │    │
│  └─────────────┘    └─────────────┘    └─────────────┘    │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 📦 Pré-requisitos

### Backend
- Java 17+
- Maven 3.8+
- PostgreSQL 15+
- Docker (opcional, para testes)

### Frontend
- Node.js 18+
- npm 9+
- Angular CLI 17+

---

## 🚀 Instalação

### 1. Clone o repositório

```bash
git clone https://github.com/seu-usuario/sge-backend.git
cd sge-backend
```

### 2. Configure o banco de dados

```bash
# Crie o banco PostgreSQL
createdb sge

# Ou use Docker
docker run -d --name sge-postgres \
  -e POSTGRES_DB=sge \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:15.17
```

### 3. Configure as variáveis de ambiente

```bash
# Copie o arquivo de exemplo
cp .env.example .env

# Edite com suas configurações
nano .env
```

### 4. Inicie o Backend

```bash
cd sge-backend

# Instale dependências
mvn clean install

# Execute o aplicativo
mvn spring-boot:run
```

### 5. Inicie o Frontend

```bash
cd sg-frontend

# Instale dependências
npm install

# Execute o aplicativo
ng serve
```

Acesse:
- **Admin**: http://localhost:4200
- **MeuMinisterio**: http://localhost:4300
- **API**: http://localhost:8080

---

## ⚙️ Configuração

### Variáveis de Ambiente

| Variável | Descrição | Padrão |
|----------|-----------|--------|
| `DATABASE_URL` | URL do banco PostgreSQL | `jdbc:postgresql://localhost:5432/sge` |
| `DATABASE_USERNAME` | Usuário do banco | `postgres` |
| `DATABASE_PASSWORD` | Senha do banco | `postgres` |
| `JWT_SECRET` | Segredo JWT (>= 32 chars) | Obrigatório |
| `JWT_EXPIRATION` | Tempo de expiração do token | `86400000` (24h) |
| `APP_BASE_URL` | URL base da aplicação | `http://localhost:4200` |
| `MAIL_HOST` | Servidor SMTP | `smtp.gmail.com` |
| `MAIL_USERNAME` | Email do remetente | Obrigatório |
| `MAIL_PASSWORD` | Senha do email | Obrigatório |

### Configuração de Upload

```yaml
app:
  upload:
    dir: uploads
    max-size: 5242880  # 5MB
    allowed-types: image/jpeg,image/png,image/gif,image/webp
```

### Configuração de Logs

```yaml
logging:
  level:
    root: INFO
    com.sg: DEBUG
  file:
    name: logs/sge-backend.log
```

---

## 📚 Uso

### Login Inicial

```
Email: admin@icert.local
Senha: 123
```

> ⚠️ Altere a senha após o primeiro login!

### Funcionalidades Principais

#### 1. Gestão de Membros
- Cadastro completo com dados pessoais
- Upload de foto com compressão automática
- Status (ativo/inativo)
- Vinculação com ministérios

#### 2. Escalas de Louvor
- Criação de escalas por data
- Designação de membros por função
- Confirmação de disponibilidade
- Geração de links públicos

#### 3. Financeiro
- Contas a pagar e receber
- Status de pagamento
- Relatórios financeiros

#### 4. Relatórios
- Exportação em PDF e Excel
- Relatórios de membros, financeiro e escalas

---

## 📖 API Documentation

### Endpoints Principais

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/auth/login` | Autenticar usuário |
| GET | `/api/usuarios` | Listar usuários |
| POST | `/api/usuarios` | Criar usuário |
| GET | `/api/membros` | Listar membros |
| POST | `/api/membros` | Criar membro |
| GET | `/api/escalas` | Listar escalas |
| POST | `/api/escalas` | Criar escala |
| GET | `/api/reports/members/pdf` | Relatório de membros (PDF) |

### Swagger

Acesse a documentação interativa:
- http://localhost:8080/swagger-ui.html
- http://localhost:8080/api-docs

---

## 🚢 Deploy

### Backend

```bash
# Build
mvn clean package -DskipTests

# Executar
java -jar target/sge-backend-1.0.0.jar \
  --spring.profiles.active=prod \
  --DATABASE_URL=jdbc:postgresql://localhost:5432/sge \
  --DATABASE_USERNAME=sge_user \
  --DATABASE_PASSWORD=senhasegura \
  --JWT_SECRET=seu_secret_aqui_mais_de_32_caracteres
```

### Frontend

```bash
# Build para produção
ng build --configuration production

# Os arquivos estarão em dist/sg-frontend/
```

### Docker

```bash
# Build da imagem
docker build -t sge-backend .

# Executar
docker run -d \
  --name sge-backend \
  -p 8080:8080 \
  -e DATABASE_URL=jdbc:postgresql://host:5432/sge \
  -e JWT_SECRET=seu_secret \
  sge-backend
```

---

## 🧪 Testes

### Backend

```bash
# Testes unitários
mvn test

# Testes de integração
mvn test -Dtest="*IntegrationTest"

# Cobertura
mvn clean test jacoco:report
```

### Frontend

```bash
# Testes unitários
ng test

# Testes E2E
npm run test:e2e

# Todos os testes
npm run test:all
```

---

## 📊 Monitoramento

### Health Checks

```bash
# Verificar saúde da aplicação
curl http://localhost:8080/actuator/health

# Métricas Prometheus
curl http://localhost:8080/actuator/prometheus
```

### Logs

```bash
# Logs em tempo real
tail -f logs/sge-backend.log

# Logs JSON
tail -f logs/sge-backend.json
```

---

## 🤝 Contribuição

1. Fork o projeto
2. Crie uma branch (`git checkout -b feature/nova-feature`)
3. Commit suas alterações (`git commit -m 'Adiciona nova feature'`)
4. Push para a branch (`git push origin feature/nova-feature`)
5. Abra um Pull Request

---

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

---

## 📞 Contato

- **Suporte**: suporte@icert.local
- **Documentação**: https://docs.icert.local
- **Issues**: https://github.com/seu-usuario/sge-backend/issues

---

## 🙏 Agradecimentos

- Equipe de Desenvolvimento ICERT
- Todos os contribuidores do projeto

---

**Última atualização:** Agosto 2026
